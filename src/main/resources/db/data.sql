-- =============================================================
-- Ascendant Initiative — Seed Data
-- Version: 1.0 | Run order: 02-data.sql (after schema.sql)
-- =============================================================

-- Admin auth is env-based only (ADMIN_EMAIL / ADMIN_PASSWORD). No admin user in DB.

-- =============================================================
-- SEED MISSIONS
-- =============================================================

-- M-01: The Lemonade Stand (ANALYTICAL · Difficulty 2)
INSERT INTO missions (id, title, narrative, difficulty_level, mission_type,
                      rule_weight, ai_weight, attribute_weights, is_active)
VALUES (
  'a1000000-0000-0000-0000-000000000001',
  'The Lemonade Stand',
  'Riya is 10 years old and wants to start a small lemonade stand in her apartment complex during the summer holidays. She has saved ₹200 from her birthday money and is excited to try running her own business.

She has done some research and found out that lemons cost ₹5 each, a bag of sugar costs ₹20 and can make 10 cups, and she can use her family''s tap water for free. Each batch of 10 cups uses 5 lemons and 1 bag of sugar.

She is thinking of selling each cup for ₹15. Her mother warned her that the weather might be unpredictable, and her father asked her what she would do if nobody bought her lemonade on the first day.

Riya wants to make a profit — but she also does not want to lose her savings. She needs your help to figure out her plan.',
  2, 'ANALYTICAL', 0.3, 0.7,
  '{"intellect": 0.25, "judgment": 0.35, "awareness": 0.25, "clarity": 0.15}',
  TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO scenarios (id, mission_id, context, choices, open_response, order_index)
VALUES (
  'b1000000-0000-0000-0000-000000000001',
  'a1000000-0000-0000-0000-000000000001',
  'Riya checks the numbers. Lemons cost ₹5 each. Sugar costs ₹20 per bag. One batch of 10 cups needs 5 lemons (₹25) + 1 sugar bag (₹20) = ₹45 total cost. If she sells at ₹15 per cup: revenue = ₹150, profit = ₹105 per batch.

But she only has ₹200. She could make 4 batches max if she sells everything. However, she is worried: what if it rains? What if people don''t want to pay ₹15 and want a cheaper price? What if she makes too much lemonade and it goes to waste?

Help Riya make her plan. Think about: how much should she make on the first day? What price makes sense? What could go wrong, and how should she handle it?',
  NULL, TRUE, 1
) ON CONFLICT DO NOTHING;

-- M-02: The Broken Bridge (FACTUAL · Difficulty 1)
INSERT INTO missions (id, title, narrative, difficulty_level, mission_type,
                      rule_weight, ai_weight, attribute_weights, is_active)
VALUES (
  'a1000000-0000-0000-0000-000000000002',
  'The Broken Bridge',
  'In a small village, there is one bridge that connects the village to the main road. Every morning, farmers use this bridge to carry their vegetables to the market. Every evening, children use it to walk home from school.

One morning, part of the bridge broke. Three wooden planks in the middle are now missing. The bridge is still standing but it is dangerous to cross. The nearest other route is 5 kilometres away — a very long walk.

The village has no money to hire a professional repair company. They have some tools, some spare wood planks, some rope, and about 20 adults who are willing to help. They have one day to fix it before the school children need to cross in the evening.',
  1, 'FACTUAL', 0.6, 0.4,
  '{"intellect": 0.20, "judgment": 0.30, "awareness": 0.30, "clarity": 0.20}',
  TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO scenarios (id, mission_id, context, choices, open_response, order_index)
VALUES (
  'b1000000-0000-0000-0000-000000000002',
  'a1000000-0000-0000-0000-000000000002',
  'The village leader calls everyone together. They have: 8 wooden planks (each 2 metres long), 50 metres of strong rope, basic carpentry tools (hammer, nails, saw), and 20 adults available for the whole day.

The bridge gap is about 3 metres wide. The planks need to be secured properly so they don''t slip when someone walks over them. They also need to make sure children can safely cross — not just adults.

Two people in the village have ideas. Person A says: "Just put the planks across and tie them with rope — it will be quick." Person B says: "We should first check the rest of the bridge to make sure nothing else is weak before we fix the middle."

Who is right? What should the village do, step by step, to fix the bridge safely before evening?',
  NULL, TRUE, 1
) ON CONFLICT DO NOTHING;

-- M-03: The Crowded Classroom (ANALYTICAL · Difficulty 3)
INSERT INTO missions (id, title, narrative, difficulty_level, mission_type,
                      rule_weight, ai_weight, attribute_weights, is_active)
VALUES (
  'a1000000-0000-0000-0000-000000000003',
  'The Crowded Classroom',
  'Greenfield School has a problem. Class 6B has 42 students but only 35 chairs and desks. Every morning, when students arrive, 7 of them have to sit on the floor or share desks. The teacher, Mrs Sharma, is frustrated. The students who sit on the floor cannot write properly and often fall behind in class.

The school principal said there is no budget to buy new furniture this year. The school''s storage room has some old broken desks, but they would need repairs. There is also an empty room next door that is used only twice a week for art class.

Mrs Sharma has 15 minutes before school starts. She needs to solve this problem today — and also think about a more permanent solution.',
  3, 'ANALYTICAL', 0.3, 0.7,
  '{"intellect": 0.30, "judgment": 0.30, "awareness": 0.25, "clarity": 0.15}',
  TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO scenarios (id, mission_id, context, choices, open_response, order_index)
VALUES (
  'b1000000-0000-0000-0000-000000000003',
  'a1000000-0000-0000-0000-000000000003',
  'Mrs Sharma looks at her options:

Option 1: Ask 7 students to go sit in the empty art room next door and give them work to do independently. But then she cannot teach them properly.

Option 2: Try to repair the broken desks in the storage room. But this would take time and she does not know how many can actually be fixed.

Option 3: Split the class into two groups — one group has class in the morning, another in the afternoon. This would solve the space problem but double her teaching hours.

Option 4: Ask students who live close by to take turns bringing a chair from home.

Think about this carefully. Which option — or which combination of options — is best for the students right now, and also in the long run? What are the trade-offs? Is there a solution Mrs Sharma might not have thought of?',
  NULL, TRUE, 1
) ON CONFLICT DO NOTHING;

-- M-04: The AI Assistant (OPEN_ENDED · Difficulty 2)
INSERT INTO missions (id, title, narrative, difficulty_level, mission_type,
                      rule_weight, ai_weight, attribute_weights, is_active)
VALUES (
  'a1000000-0000-0000-0000-000000000004',
  'The AI Assistant',
  'Arjun is 11 years old and loves using AI tools to help him with his homework and projects. One day, his teacher gave the class a project: "Write a short report on why bees are important."

Arjun typed the question into an AI chatbot and got back a very detailed, confident-sounding answer. The AI said: "Bees are important because they produce honey, which humans eat. They also help flowers look pretty. Some scientists believe bees are becoming more common every year because of better farming practices."

Arjun was about to copy the answer into his report. But something felt off.',
  2, 'OPEN_ENDED', 0.15, 0.85,
  '{"intellect": 0.30, "judgment": 0.20, "awareness": 0.30, "clarity": 0.20}',
  TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO scenarios (id, mission_id, context, choices, open_response, order_index)
VALUES (
  'b1000000-0000-0000-0000-000000000004',
  'a1000000-0000-0000-0000-000000000004',
  'Read the AI''s answer carefully: "Bees are important because they produce honey, which humans eat. They also help flowers look pretty. Some scientists believe bees are becoming more common every year because of better farming practices."

There are at least 2 things in this answer that are wrong or misleading. There is also something very important about bees that the AI completely left out.

Your challenge:
1. What do you think might be wrong or missing in the AI''s answer?
2. How would you check if the AI is telling the truth?
3. Should Arjun trust this answer and copy it? Why or why not?
4. What is a smarter way for Arjun to use AI for his homework?

Think carefully. The AI sounded very confident — but confidence does not mean correctness.',
  NULL, TRUE, 1
) ON CONFLICT DO NOTHING;

-- M-05: The Village Water Problem (OPEN_ENDED · Difficulty 4)
INSERT INTO missions (id, title, narrative, difficulty_level, mission_type,
                      rule_weight, ai_weight, attribute_weights, is_active)
VALUES (
  'a1000000-0000-0000-0000-000000000005',
  'The Village Water Problem',
  'In a village called Rampur, 500 people depend on one well for all their water. Every morning, women and children walk up to 2 kilometres to collect water in large pots. This takes 3–4 hours each day — time that could be used for school, work, or rest.

The well is old and during summer, it dries up for about 6 weeks. During those weeks, the village buys water from a tanker truck that charges ₹500 per visit, and they need it 3 times a week. This costs ₹6,000 per month — money the village barely has.

A government engineer visited and offered two solutions. Solution A: Install a hand pump near the well (cost: ₹15,000, lasts 10 years). Solution B: Build a small rainwater harvesting tank on the hill above the village (cost: ₹40,000, but could last 25 years and provide water even in summer).

The village council has saved ₹20,000. They need to make a decision.',
  4, 'OPEN_ENDED', 0.15, 0.85,
  '{"intellect": 0.25, "judgment": 0.30, "awareness": 0.30, "clarity": 0.15}',
  TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO scenarios (id, mission_id, context, choices, open_response, order_index)
VALUES (
  'b1000000-0000-0000-0000-000000000005',
  'a1000000-0000-0000-0000-000000000005',
  'The village council is meeting tonight. Here are the facts:

Current savings: ₹20,000
Solution A (Hand pump): ₹15,000 · Solves the daily walking problem · Does NOT solve the summer dry spell
Solution B (Rainwater tank): ₹40,000 · Solves both problems · But they are ₹20,000 short

Some council members say: "Take Solution A now, it is affordable."
Others say: "Solution A is a waste — it does not fix the summer problem. We should wait and save more money for Solution B."
A young woman in the village says: "What if we do Solution A now AND start a savings plan for Solution B?"

Think about this deeply:
1. What are the real costs of doing nothing? (think beyond just money)
2. Is Solution A actually a waste, or does it have value right now?
3. How could the village raise the remaining ₹20,000 for Solution B?
4. What would you recommend, and why? Consider everyone in the village — not just the council.',
  NULL, TRUE, 1
) ON CONFLICT DO NOTHING;

-- =============================================================
-- Done
-- =============================================================
-- Seeds loaded: 1 admin user + 5 missions + 5 scenarios
