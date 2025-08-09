SHOW TABLES;

CREATE TABLE Usuario (
                         id BIGINT PRIMARY KEY,
                         nome TEXT NOT NULL,
                         codigo_acesso TEXT NOT NULL,
                         tipo_usuario VARCHAR(255)
);

CREATE TABLE Administrador (
                               id BIGINT PRIMARY KEY,
                               endereco TEXT,
                               FOREIGN KEY (id) REFERENCES Usuario(id)
);

CREATE TABLE tipo_de_ativo(
    tipo TEXT
);

INSERT INTO Usuario (id, nome, codigo_acesso, tipo_usuario) VALUES (0, 'sicrano', '654321', 'ADMINISTRADOR');
INSERT INTO Administrador (id, endereco) VALUES (0, 'rua aquela');

INSERT INTO tipo_de_ativo (tipo) VALUES ('TESOURO_DIRETO');
INSERT INTO tipo_de_ativo (tipo) VALUES ('CRIPTOMOEDA');
INSERT INTO tipo_de_ativo (tipo) VALUES ('ACAO');