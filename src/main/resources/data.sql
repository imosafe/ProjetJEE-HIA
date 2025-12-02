-- 1. Insérer l'Admin (UNIQUEMENT s'il n'existe pas déjà, plus de DELETE brutal)
INSERT INTO app_users (username, password, role)
SELECT 'admin', 'admin123', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

-- 2. Insérer l'équipe T1
INSERT INTO teams (name, logo_url)
SELECT 'T1', 'http://logo1.png'
WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'T1');

-- 3. Insérer l'équipe G2
INSERT INTO teams (name, logo_url)
SELECT 'G2 Esports', 'http://logo2.png'
WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'G2 Esports');

-- ============================================================
-- 4. DONNÉES DE TEST SPONSORS (Avec les nouvelles colonnes)

-- Red Bull (Boissons, GOLD)
INSERT INTO sponsor (name, logo_url, website_url, type, level) 
SELECT 'Red Bull', 'https://upload.wikimedia.org/wikipedia/en/thumb/f/f5/RedBullEnergyDrink.svg/1200px-RedBullEnergyDrink.svg.png', 'https://www.redbull.com', 'DRINKS', 'GOLD'
WHERE NOT EXISTS (SELECT 1 FROM sponsor WHERE name = 'Red Bull');

-- Logitech (Matériel, SILVER)
INSERT INTO sponsor (name, logo_url, website_url, type, level) 
SELECT 'Logitech G', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/Logitech_logo.svg/2560px-Logitech_logo.svg.png', 'https://www.logitechg.com', 'HARDWARE', 'SILVER'
WHERE NOT EXISTS (SELECT 1 FROM sponsor WHERE name = 'Logitech G');

-- Secret Lab (Autre, BRONZE)
INSERT INTO sponsor (name, logo_url, website_url, type, level) 
SELECT 'Secret Lab', 'https://upload.wikimedia.org/wikipedia/commons/8/8b/Secretlab_Logo_2020.png', 'https://secretlab.eu', 'OTHER', 'BRONZE'
WHERE NOT EXISTS (SELECT 1 FROM sponsor WHERE name = 'Secret Lab');