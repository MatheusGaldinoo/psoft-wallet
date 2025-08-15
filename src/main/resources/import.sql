SHOW TABLES;

INSERT INTO Usuario (id, nome, codigo_acesso, tipo_usuario) VALUES (0, 'sicrano', '654321', 'ADMINISTRADOR');
INSERT INTO Administrador (id) VALUES (0);

INSERT INTO tipo_de_ativo (tipo, nome_tipo) VALUES ('TESOURO_DIRETO', 'TESOURO_DIRETO');
INSERT INTO tipo_de_ativo (tipo, nome_tipo) VALUES ('CRIPTOMOEDA', 'CRIPTOMOEDA');
INSERT INTO tipo_de_ativo (tipo, nome_tipo) VALUES ('ACAO', 'ACAO');