-- 1. Insérer l'Admin (si pas déjà là)
INSERT INTO app_users (username, password, role)
SELECT 'admin', 'admin123', 'ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

-- 2. Insérer l'équipe T1 (si pas déjà là)
INSERT INTO teams (name, logo_url)
SELECT 'T1', 'http://logo1.png'
    WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'T1');

-- 3. Insérer l'équipe G2 (si pas déjà là)
INSERT INTO teams (name, logo_url)
SELECT 'G2 Esports', 'http://logo2.png'
    WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'G2 Esports');