package com.ascendant.initiative.engine.rule;

import com.ascendant.initiative.model.Mission;
import com.ascendant.initiative.model.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RuleEngineTest {

    private LengthScorer lengthScorer;
    private KeywordDensityScorer keywordDensityScorer;
    private LogicalConnectorScorer logicalConnectorScorer;
    private HypothesisDetector hypothesisDetector;
    private ScoreNormalizer scoreNormalizer;
    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        lengthScorer          = new LengthScorer();
        keywordDensityScorer  = new KeywordDensityScorer();
        logicalConnectorScorer = new LogicalConnectorScorer();
        hypothesisDetector    = new HypothesisDetector();
        scoreNormalizer       = new ScoreNormalizer();
        ruleEngine = new RuleEngine(lengthScorer, keywordDensityScorer,
                                    logicalConnectorScorer, hypothesisDetector, scoreNormalizer);
    }

    // ── LengthScorer ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("LengthScorer")
    class LengthScorerTests {

        @Test
        @DisplayName("Empty text → score = 0.0")
        void emptyText() {
            assertThat(lengthScorer.score("", 1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Difficulty 1, 30+ words → score = 1.0")
        void difficulty1FullScore() {
            String text = "a ".repeat(30).trim();
            assertThat(lengthScorer.score(text, 1)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Difficulty 1, 5 words → score = 0.0 (below zero threshold)")
        void difficulty1ZeroScore() {
            assertThat(lengthScorer.score("one two three four five", 1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Difficulty 3, 80+ words → score = 1.0")
        void difficulty3FullScore() {
            String text = "word ".repeat(80).trim();
            assertThat(lengthScorer.score(text, 3)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Difficulty 5, 150+ words → score = 1.0")
        void difficulty5FullScore() {
            String text = "word ".repeat(150).trim();
            assertThat(lengthScorer.score(text, 5)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Mid-range score is proportional")
        void midRangeScore() {
            // Difficulty 1: zero=10, full=30 → 20 words should be ~0.5
            String text = "word ".repeat(20).trim();
            double score = lengthScorer.score(text, 1);
            assertThat(score).isBetween(0.4, 0.6);
        }

        @Test
        @DisplayName("Score is always in [0.0, 1.0]")
        void scoreBounds() {
            assertThat(lengthScorer.score("word ".repeat(500).trim(), 5)).isLessThanOrEqualTo(1.0);
            assertThat(lengthScorer.score("", 5)).isGreaterThanOrEqualTo(0.0);
        }
    }

    // ── KeywordDensityScorer ──────────────────────────────────────────────────

    @Nested
    @DisplayName("KeywordDensityScorer")
    class KeywordDensityScorerTests {

        @Test
        @DisplayName("No reasoning keywords → 0.0")
        void noKeywords() {
            assertThat(keywordDensityScorer.score("The cat sat on the mat and then it ran away", 1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("'because' present in short text → positive score")
        void singleKeywordShortText() {
            double score = keywordDensityScorer.score("I chose this because it is better", 1);
            assertThat(score).isPositive();
        }

        @Test
        @DisplayName("High density of keywords → score = 1.0")
        void highDensity() {
            String text = "because therefore since therefore because because because therefore";
            assertThat(keywordDensityScorer.score(text, 1)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Score is always in [0.0, 1.0]")
        void scoreBounds() {
            String dense = "because ".repeat(100).trim();
            assertThat(keywordDensityScorer.score(dense, 1)).isLessThanOrEqualTo(1.0);
        }

        @Test
        @DisplayName("Keyword density beats keyword count — same keyword sparse hurts score")
        void densityOverCount() {
            String sparse = "because " + "word ".repeat(200).trim();
            String dense = "because therefore since " + "word ".repeat(10).trim();
            assertThat(keywordDensityScorer.score(dense, 2))
                    .isGreaterThan(keywordDensityScorer.score(sparse, 2));
        }
    }

    // ── LogicalConnectorScorer ────────────────────────────────────────────────

    @Nested
    @DisplayName("LogicalConnectorScorer")
    class LogicalConnectorScorerTests {

        @Test
        @DisplayName("No connectors → 0.0")
        void noConnectors() {
            assertThat(logicalConnectorScorer.score("The sky is blue and birds fly high")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("'However' present → positive score")
        void contrastConnector() {
            assertThat(logicalConnectorScorer.score("She wanted to go however she was tired")).isPositive();
        }

        @Test
        @DisplayName("If-then pattern detected → high causation score")
        void ifThenPattern() {
            String text = "If it rains tomorrow then the match will be cancelled";
            assertThat(logicalConnectorScorer.score(text)).isGreaterThan(0.5);
        }

        @Test
        @DisplayName("Both contrast AND if-then → score near 1.0")
        void bothTypes() {
            String text = "If the price is too high then customers will leave. However we need profit.";
            assertThat(logicalConnectorScorer.score(text)).isGreaterThan(0.7);
        }

        @Test
        @DisplayName("Score capped at 1.0")
        void scoreCapped() {
            String rich = "If A then B. If C then D. However despite although nevertheless whereas";
            assertThat(logicalConnectorScorer.score(rich)).isLessThanOrEqualTo(1.0);
        }
    }

    // ── HypothesisDetector ────────────────────────────────────────────────────

    @Nested
    @DisplayName("HypothesisDetector")
    class HypothesisDetectorTests {

        @Test
        @DisplayName("If-then pair → score = 1.0")
        void ifThenFull() {
            assertThat(hypothesisDetector.score("If it rains tomorrow then I will stay home"))
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("'What if' phrase → score = 0.8")
        void whatIf() {
            assertThat(hypothesisDetector.score("What if we tried a different approach?"))
                    .isEqualTo(0.8);
        }

        @Test
        @DisplayName("Question mark present → score = 0.5")
        void questionMark() {
            assertThat(hypothesisDetector.score("Could this work? I think it might"))
                    .isEqualTo(0.5);
        }

        @Test
        @DisplayName("No hypothesis markers → 0.0")
        void noMarkers() {
            assertThat(hypothesisDetector.score("The answer is definitely 42")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("If without then → no causation bonus")
        void ifWithoutThen() {
            double score = hypothesisDetector.score("If only I could go to the park today");
            assertThat(score).isLessThan(1.0);
        }
    }

    // ── ScoreNormalizer ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("ScoreNormalizer")
    class ScoreNormalizerTests {

        @Test
        @DisplayName("All 1.0 → 1.0")
        void allOnes() {
            assertThat(scoreNormalizer.normalize(1.0, 1.0, 1.0, 1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("All 0.0 → 0.0")
        void allZeros() {
            assertThat(scoreNormalizer.normalize(0.0, 0.0, 0.0, 0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Result always in [0.0, 1.0]")
        void boundsCheck() {
            assertThat(scoreNormalizer.normalize(2.0, 2.0, 2.0, 2.0)).isLessThanOrEqualTo(1.0);
            assertThat(scoreNormalizer.normalize(-1.0, -1.0, -1.0, -1.0)).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Weights sum verified — mixed input produces expected range")
        void mixedInput() {
            double result = scoreNormalizer.normalize(0.5, 0.5, 0.5, 0.5);
            assertThat(result).isCloseTo(0.5, within(0.01));
        }
    }

    // ── Full RuleEngine Integration ────────────────────────────────────────────

    @Nested
    @DisplayName("RuleEngine Integration")
    class RuleEngineIntegrationTests {

        @Test
        @DisplayName("Strong response gets high score")
        void strongResponse() {
            String strong = "I think Riya should price it at fifteen rupees because she needs to cover " +
                "her costs. If she sells ten cups then she makes one hundred fifty rupees. However, " +
                "if it rains she might sell fewer cups therefore she should have a backup plan. " +
                "Since her total costs are fifty five rupees, her profit would be ninety five rupees. " +
                "I would also think about what if customers want a cheaper option because then she " +
                "might lose sales. This leads to the conclusion that she needs to be flexible with pricing.";
            var result = ruleEngine.score(mockResponse(strong), mockMission(2));
            assertThat(result.finalScore()).isGreaterThan(0.6);
        }

        @Test
        @DisplayName("Weak response gets low score")
        void weakResponse() {
            var result = ruleEngine.score(mockResponse("I think she should sell it for 15 rupees"), mockMission(2));
            assertThat(result.finalScore()).isLessThan(0.4);
        }

        @Test
        @DisplayName("Score is always in [0.0, 1.0]")
        void scoreBounds() {
            var result = ruleEngine.score(mockResponse(""), mockMission(1));
            assertThat(result.finalScore()).isBetween(0.0, 1.0);
        }

        private Response mockResponse(String text) {
            Response r = new Response();
            r.setAnswerText(text);
            return r;
        }

        private Mission mockMission(int difficulty) {
            Mission m = new Mission();
            m.setDifficultyLevel(difficulty);
            m.setMissionType(Mission.MissionType.ANALYTICAL);
            return m;
        }
    }
}
