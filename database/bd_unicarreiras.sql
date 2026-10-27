CREATE DATABASE IF NOT EXISTS db_unicarreiras
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE db_unicarreiras;

CREATE TABLE estudante (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nome                VARCHAR(150) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    senha               VARCHAR(255) NOT NULL,
    curso               VARCHAR(100) NOT NULL,
    instituicao_ensino  VARCHAR(150) NOT NULL,
    semestre            TINYINT CHECK (semestre BETWEEN 1 AND 20),
    cidade              VARCHAR(100),
    telefone            VARCHAR(20),
    data_cadastro       DATE NOT NULL DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB;

CREATE TABLE empresa (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    cnpj          VARCHAR(18) NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    senha         VARCHAR(255) NOT NULL,
    setor         VARCHAR(100),
    cidade        VARCHAR(100),
    descricao     TEXT,
    data_cadastro DATE NOT NULL DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB;

CREATE TABLE administrador (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nome   VARCHAR(150) NOT NULL,
    email  VARCHAR(150) NOT NULL UNIQUE,
    senha  VARCHAR(255) NOT NULL
) ENGINE=InnoDB;


CREATE TABLE vaga (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id        INT NOT NULL,
    titulo            VARCHAR(150) NOT NULL,
    descricao         TEXT,
    area              VARCHAR(100),
    modalidade        VARCHAR(20) CHECK (modalidade IN ('presencial', 'remoto', 'hibrido')),
    carga_horaria     TINYINT CHECK (carga_horaria > 0 AND carga_horaria <= 30),
    cidade            VARCHAR(100),
    status            VARCHAR(20) NOT NULL DEFAULT 'aberta'
                      CHECK (status IN ('aberta', 'encerrada')),
    data_publicacao   DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_vaga_empresa FOREIGN KEY (empresa_id)
        REFERENCES empresa(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_vaga_area ON vaga(area);
CREATE INDEX idx_vaga_cidade ON vaga(cidade);
CREATE INDEX idx_vaga_status ON vaga(status);


CREATE TABLE candidatura (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    estudante_id       INT NOT NULL,
    vaga_id            INT NOT NULL,
    data_candidatura   DATE NOT NULL DEFAULT (CURRENT_DATE),
    status             VARCHAR(30) NOT NULL DEFAULT 'candidatado'
                       CHECK (status IN ('candidatado', 'em_analise', 'entrevista',
                                          'aprovado', 'recusado')),
    data_entrevista    DATE,
    feedback_empresa   TEXT,
    CONSTRAINT fk_candidatura_estudante FOREIGN KEY (estudante_id)
        REFERENCES estudante(id) ON DELETE CASCADE,
    CONSTRAINT fk_candidatura_vaga FOREIGN KEY (vaga_id)
        REFERENCES vaga(id) ON DELETE CASCADE,
    UNIQUE KEY uk_candidatura_estudante_vaga (estudante_id, vaga_id)
) ENGINE=InnoDB;

CREATE INDEX idx_candidatura_estudante ON candidatura(estudante_id);
CREATE INDEX idx_candidatura_vaga ON candidatura(vaga_id);


CREATE TABLE habilidade (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;


CREATE TABLE estudante_habilidade (
    estudante_id   INT NOT NULL,
    habilidade_id  INT NOT NULL,
    PRIMARY KEY (estudante_id, habilidade_id),
    CONSTRAINT fk_eh_estudante FOREIGN KEY (estudante_id)
        REFERENCES estudante(id) ON DELETE CASCADE,
    CONSTRAINT fk_eh_habilidade FOREIGN KEY (habilidade_id)
        REFERENCES habilidade(id) ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE experiencia (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    estudante_id  INT NOT NULL,
    cargo         VARCHAR(100) NOT NULL,
    empresa_nome  VARCHAR(150) NOT NULL,
    data_inicio   DATE NOT NULL,
    data_fim      DATE,                 -- nulo = experiência em andamento
    descricao     TEXT,
    CONSTRAINT fk_experiencia_estudante FOREIGN KEY (estudante_id)
        REFERENCES estudante(id) ON DELETE CASCADE,
    CHECK (data_fim IS NULL OR data_fim >= data_inicio)
) ENGINE=InnoDB;

CREATE INDEX idx_experiencia_estudante ON experiencia(estudante_id);


CREATE TABLE curso_complementar (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    estudante_id     INT NOT NULL,
    nome             VARCHAR(150) NOT NULL,
    instituicao      VARCHAR(150),
    carga_horaria    SMALLINT CHECK (carga_horaria > 0),
    data_conclusao   DATE,
    CONSTRAINT fk_curso_estudante FOREIGN KEY (estudante_id)
        REFERENCES estudante(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_curso_estudante ON curso_complementar(estudante_id);

CREATE TABLE notificacao (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    destinatario_id     INT NOT NULL,
    tipo_destinatario   VARCHAR(10) NOT NULL CHECK (tipo_destinatario IN ('estudante', 'empresa')),
    mensagem            VARCHAR(255) NOT NULL,
    data_envio          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lida                BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE INDEX idx_notificacao_destinatario ON notificacao(destinatario_id, tipo_destinatario);

USE db_unicarreiras;

INSERT INTO estudante (nome, email, senha, curso, instituicao_ensino, semestre, cidade, telefone) VALUES
('Ana Beatriz Souza',   'ana.souza@email.com',     '$2b$hash_ana',    'Ciência da Computação', 'UnB',  6, 'Brasília',      '(61) 99111-2233'),
('Carlos Eduardo Lima', 'carlos.lima@email.com',   '$2b$hash_carlos', 'Sistemas de Informação', 'IFB',  4, 'Brasília',      '(61) 99222-3344'),
('Mariana Ferreira',    'mariana.ferreira@email.com','$2b$hash_mari', 'Engenharia de Software', 'UnB',  8, 'Taguatinga',    '(61) 99333-4455'),
('João Pedro Alves',    'joao.alves@email.com',    '$2b$hash_joao',   'Análise e Des. Sistemas','IFB',  2, 'Ceilândia',     '(61) 99444-5566'),
('Beatriz Rocha',       'beatriz.rocha@email.com', '$2b$hash_bia',    'Ciência da Computação',  'UnB',  5, 'Águas Claras',  '(61) 99555-6677');


INSERT INTO empresa (nome, cnpj, email, senha, setor, cidade, descricao) VALUES
('TechNova Soluções',      '12.345.678/0001-90', 'rh@technova.com',      '$2b$hash_technova', 'Tecnologia',      'Brasília', 'Empresa de desenvolvimento de software.'),
('Grupo Cerrado Logística','23.456.789/0001-01', 'contato@cerrado.com',  '$2b$hash_cerrado',  'Logística',       'Brasília', 'Logística e transporte de cargas.'),
('Banco Planalto',         '34.567.890/0001-12', 'estagios@planalto.com','$2b$hash_planalto', 'Financeiro',      'Brasília', 'Instituição financeira regional.'),
('EduTech Cursos',         '45.678.901/0001-23', 'rh@edutech.com',       '$2b$hash_edutech',  'Educação',        'Brasília', 'Plataforma de cursos online.');


INSERT INTO administrador (nome, email, senha) VALUES
('Administrador do Sistema', 'admin@sistemaestagios.com', '$2b$hash_admin');


INSERT INTO vaga (empresa_id, titulo, descricao, area, modalidade, carga_horaria, cidade, status, data_publicacao) VALUES
(1, 'Estágio em Desenvolvimento Backend',  'Apoio no desenvolvimento de APIs em Node.js.', 'Tecnologia',   'hibrido',    30, 'Brasília', 'aberta',    '2026-08-01'),
(1, 'Estágio em Suporte de TI',            'Atendimento e suporte técnico interno.',       'Tecnologia',   'presencial', 20, 'Brasília', 'aberta',    '2026-08-05'),
(2, 'Estágio em Logística',                'Controle de estoque e rotas de entrega.',      'Logística',    'presencial', 25, 'Brasília', 'aberta',    '2026-07-20'),
(3, 'Estágio em Análise de Dados',         'Apoio à área de BI e relatórios financeiros.', 'Dados',        'hibrido',    30, 'Brasília', 'aberta',    '2026-08-10'),
(3, 'Estágio Administrativo',              'Rotinas administrativas do setor financeiro.', 'Administração','presencial', 20, 'Brasília', 'encerrada', '2026-06-15'),
(4, 'Estágio em Design Instrucional',      'Apoio na criação de conteúdo para cursos.',    'Educação',     'remoto',     20, 'Brasília', 'aberta',    '2026-08-12');


INSERT INTO candidatura (estudante_id, vaga_id, data_candidatura, status, data_entrevista, feedback_empresa) VALUES
(1, 1, '2026-08-02', 'entrevista',   '2026-08-20', NULL),
(1, 4, '2026-08-11', 'candidatado',  NULL,         NULL),
(2, 2, '2026-08-06', 'em_analise',   NULL,         NULL),
(3, 1, '2026-08-03', 'aprovado',     '2026-08-15', 'Perfil técnico muito bom, aprovado.'),
(3, 3, '2026-07-21', 'recusado',     NULL,         'Vaga preenchida por outro candidato.'),
(4, 6, '2026-08-13', 'candidatado',  NULL,         NULL),
(5, 4, '2026-08-12', 'entrevista',   '2026-08-22', NULL);


INSERT INTO habilidade (nome) VALUES
('JavaScript'), ('Python'), ('SQL'), ('Excel'), ('Comunicação'), ('Power BI'), ('Node.js'), ('Figma');


-- =========================================================
INSERT INTO estudante_habilidade (estudante_id, habilidade_id) VALUES
(1, 1), (1, 7), (1, 3),
(2, 3), (2, 4),
(3, 2), (3, 3), (3, 6),
(4, 4), (4, 5),
(5, 2), (5, 6), (5, 8);

INSERT INTO experiencia (estudante_id, cargo, empresa_nome, data_inicio, data_fim, descricao) VALUES
(1, 'Monitor de laboratório', 'UnB',                '2025-03-01', '2025-12-15', 'Monitoria em disciplina de estrutura de dados.'),
(3, 'Estagiária de TI',       'InovaTech Ltda',     '2024-08-01', '2025-06-30', 'Suporte a sistemas internos e desenvolvimento de scripts.'),
(5, 'Auxiliar de pesquisa',   'Laboratório de IA - UnB', '2025-02-01', NULL,     'Pesquisa em modelos de recomendação, atividade em andamento.');


INSERT INTO curso_complementar (estudante_id, nome, instituicao, carga_horaria, data_conclusao) VALUES
(1, 'Formação Node.js',         'Alura',       40, '2025-11-20'),
(2, 'Excel Avançado',           'Senac',       20, '2025-05-10'),
(3, 'Python para Data Science', 'Coursera',    60, '2025-09-01'),
(5, 'Power BI Essencial',       'Udemy',       30, '2025-10-05');

INSERT INTO notificacao (destinatario_id, tipo_destinatario, mensagem, data_envio, lida) VALUES
(1, 'estudante', 'Sua candidatura para "Estágio em Desenvolvimento Backend" avançou para a fase de entrevista.', '2026-08-14 09:00:00', FALSE),
(3, 'estudante', 'Parabéns! Você foi aprovado para "Estágio em Desenvolvimento Backend".',                       '2026-08-16 10:30:00', TRUE),
(3, 'estudante', 'Sua candidatura para "Estágio em Análise de Dados" foi encerrada.',                            '2026-07-22 14:00:00', TRUE),
(1, 'empresa',   'Nova candidatura recebida para "Estágio em Desenvolvimento Backend".',                         '2026-08-02 11:15:00', TRUE),
(3, 'empresa',   'Nova candidatura recebida para "Estágio em Análise de Dados".',                                '2026-08-12 08:45:00', FALSE);

SELECT * FROM curso_complementar;

select * FROM habilidade;