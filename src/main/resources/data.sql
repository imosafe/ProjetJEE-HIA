-- 1. Insérer l'Admin
INSERT INTO app_users (username, password, role)
SELECT 'admin', 'admin123', 'ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

-- 2. Insérer l'équipe T1 (Avec le jeu !)
INSERT INTO teams (name, logo_url, game)
SELECT 'T1', 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/T1_logo.svg/1200px-T1_logo.svg.png', 'League of Legends'
    WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'T1');

-- 3. Insérer l'équipe G2 (Avec le jeu !)
INSERT INTO teams (name, logo_url, game)
SELECT 'G2 Esports', 'https://upload.wikimedia.org/wikipedia/en/thumb/1/12/Esports_G2_logo.svg/1200px-Esports_G2_logo.svg.png', 'League of Legends'
    WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'G2 Esports');

-- 4. Une équipe Valorant pour tester le filtre
INSERT INTO teams (name, logo_url, game)
SELECT 'Sentinels', 'https://upload.wikimedia.org/wikipedia/en/thumb/7/7d/Sentinels_logo.svg/1200px-Sentinels_logo.svg.png', 'Valorant'
    WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'Sentinels');