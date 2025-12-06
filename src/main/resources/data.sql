-- 1. Insérer l'Admin et les Joueurs
INSERT INTO app_users (username, password, role) 
SELECT 'admin', 'admin', 'ADMIN' 
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

INSERT INTO app_users (username, password, role) 
SELECT 'faker', '1234', 'PLAYER' 
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'faker');

INSERT INTO app_users (username, password, role) 
SELECT 'tenz', '1234', 'PLAYER' 
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'tenz');


-- 2. Insérer l'équipe T1 (Jeu = LOL)
-- On ajoute 'leader_id' pour éviter les bugs dans l'affichage HTML
INSERT INTO teams (name, logo_url, game, leader_id)
SELECT 'T1', 
       'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/T1_logo.svg/1200px-T1_logo.svg.png', 
       'LOL', -- 👈 ICI : Le code Enum, pas le nom complet !
       (SELECT id FROM app_users WHERE username = 'faker')
WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'T1');


-- 3. Insérer l'équipe G2 (Jeu = LOL)
INSERT INTO teams (name, logo_url, game)
SELECT 'G2 Esports', 
       'https://upload.wikimedia.org/wikipedia/en/thumb/1/12/G2_Esports_logo.svg/1200px-G2_Esports_logo.svg.png', 
       'LOL' -- 👈 ICI : Le code Enum
WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'G2 Esports');


-- 4. Une équipe Valorant (Jeu = VALORANT)
INSERT INTO teams (name, logo_url, game, leader_id)
SELECT 'Sentinels', 
       'https://upload.wikimedia.org/wikipedia/en/thumb/7/7d/Sentinels_logo.svg/1200px-Sentinels_logo.svg.png', 
       'VALORANT', -- 👈 ICI : Le code Enum
       (SELECT id FROM app_users WHERE username = 'tenz')
WHERE NOT EXISTS (SELECT 1 FROM teams WHERE name = 'Sentinels');


-- 5. Mise à jour des User pour lier à l'équipe (Bidirectionnel)
UPDATE app_users SET team_id = (SELECT id FROM teams WHERE name = 'T1') WHERE username = 'faker';
UPDATE app_users SET team_id = (SELECT id FROM teams WHERE name = 'Sentinels') WHERE username = 'tenz';