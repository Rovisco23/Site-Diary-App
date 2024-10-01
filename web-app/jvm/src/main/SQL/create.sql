drop table if exists INTERVENIENTE;
drop table if exists CONVITE;
drop table if exists DOCUMENTO;
drop table if exists IMAGEM;
drop table if exists IMAGEM_OBRA;
drop table if exists REGISTO;
drop table if exists MEMBRO;
drop table if exists TERMO_ABERTURA;
drop table if exists EMPRESA_CONSTRUCAO;
drop table if exists OBRA;
drop table if exists SESSAO;
drop table if exists PROFILE_PICTURE;
drop table if exists UTILIZADOR;
drop table if exists LOCALIDADE;

create table UTILIZADOR
(
    id                serial,
    email             varchar(255) unique not null,
    username          varchar(255) unique,
    nif               integer unique,
    role              varchar(8),
    password          varchar(256),
    nome              varchar(50),
    apelido           varchar(50),
    associacao_nome   varchar(255),
    associacao_numero integer,
    telefone          varchar(9),
    freguesia         varchar(255),
    concelho          varchar(255),
    distrito          varchar(255),
    pendente          boolean,
    primary key (id),
    constraint Role_Format check (ROLE IN ('OPERÁRIO', 'CÂMARA', 'ADMIN')),
    constraint Email_Format check (email like '%@%.%'),
    CONSTRAINT check_nine_digits CHECK (char_length(nif::text) = 9)
);

create table SESSAO
(
    token_validation varchar(256),
    uId              int,
    created_at       bigint not null,
    last_used_at     bigint not null,
    primary key (uId, token_validation),
    constraint UserId foreign key (uId) references UTILIZADOR (id)
);

create table OBRA
(
    id             varchar(255),
    nome           varchar(50)  not null,
    tipo           varchar(50)  not null,
    descricao      varchar(500) not null,
    estado         varchar(50)  not null,
    data_conclusao timestamp,
    freguesia      varchar(255) not null,
    concelho       varchar(255) not null,
    distrito       varchar(255) not null,
    rua            varchar(255) not null,
    cPostal        varchar(8)   not null,
    primary key (id),
    constraint cPostal_format check (cPostal LIKE '%-%'),
    constraint Tipo CHECK (tipo IN
                           ('RESIDENCIAL', 'COMERCIAL', 'INDUSTRIAL', 'INFRAESTRUTURA', 'INSTITUCIONAL', 'REABILITAÇÃO',
                            'ESTRUTURA ESPECIAL', 'OBRA DE ARTE', 'HABITAÇÃO', 'EDIFICIO ESPECIAL')),
    constraint Estado CHECK (estado IN ('EM PROGRESSO', 'TERMINADA', 'REJEITADA', 'EM VERIFICAÇÃO'))
);

create table MEMBRO
(
    uId      integer,
    oId      varchar(255),
    role     varchar(255) not null,
    pendente boolean      not null default false,
    primary key (uId, oId),
    constraint UserId foreign key (uId) references UTILIZADOR (id),
    constraint ObraId foreign key (oId) references OBRA (id),
    constraint MEMBER_ROLE CHECK (role IN ('DONO', 'MEMBRO', 'ESPECTADOR', 'FISCALIZAÇÃO', 'COORDENADOR',
                                           'ARQUITETURA', 'ESTABILIDADE', 'ELETRICIDADE', 'GÁS', 'CANALIZAÇÃO',
                                           'TELECOMUNICAÇÕES', 'TERMICO', 'ACUSTICO',
                                           'TRANSPORTES', 'DIRETOR'))
);

create table REGISTO
(
    id                     serial,
    oId                    varchar(255),
    texto                  varchar(2500) not null,
    editable               boolean       not null,
    creation_date          timestamp     not null,
    last_modification_date timestamp,
    author                 integer,
    primary key (id, oId),
    constraint ObraId foreign key (oId) references OBRA (id),
    constraint UserId foreign key (author) references UTILIZADOR (id)
);

create table IMAGEM
(
    id          serial,
    oId         varchar(255),
    rId         integer,
    name        varchar(255) not null,
    type        varchar(255) not null,
    file        bytea        not null,
    upload_date timestamp    not null,
    primary key (id, oId, rId),
    constraint ObraId foreign key (oId) references OBRA (id),
    constraint RegistoId foreign key (rId, oId) references REGISTO (id, oId)
);

create table DOCUMENTO
(
    id          serial,
    oId         varchar(255),
    rId         integer,
    name        varchar(255) not null,
    type        varchar(255) not null,
    file        bytea        not null,
    upload_date timestamp    not null,
    primary key (id, oId, rId),
    constraint ObraId foreign key (oId) references OBRA (id),
    constraint RegistoId foreign key (rId, oId) references REGISTO (id, oId)
);

create table EMPRESA_CONSTRUCAO
(
    id     serial,
    nome   varchar(255) not null,
    numero integer      not null,
    primary key (id)
);

create table LOCALIDADE
(
    id        serial,
    distrito  varchar(255) not null,
    concelho  varchar(255) not null,
    freguesia varchar(255) not null,
    primary key (id)
);

create table TERMO_ABERTURA
(
    id                 serial,
    oId                varchar(255),
    inicio             timestamp    not null,
    camara             integer,
    titular_licenca    varchar(255) not null,
    empresa_construcao integer,
    autorizacao        varchar(255),
    assinatura         varchar(255),
    dt_assinatura      timestamp,
    predio             varchar(255) not null,
    primary key (id, oId),
    constraint ObraId foreign key (oId) references OBRA (id),
    constraint EmpresaId foreign key (empresa_construcao) references EMPRESA_CONSTRUCAO (id),
    constraint CamaraId foreign key (camara) references localidade (id)
);

create table INTERVENIENTE
(
    id         serial,
    tId        integer,
    oId        varchar(255),
    nome       varchar(255) not null,
    email      varchar(255) not null,
    role       varchar(255) not null,
    associacao varchar(255) not null,
    numero     integer      not null,
    primary key (id, tId, oId),
    constraint TermoId foreign key (tId, oId) references TERMO_ABERTURA (id, oId),
    constraint ObraId foreign key (oId) references OBRA (id),
    constraint Email_Format check (email like '%@%.%'),
    constraint RoleInterviniente CHECK (role IN
                                        ('FISCALIZAÇÃO', 'COORDENADOR', 'ARQUITETURA', 'ESTABILIDADE', 'ELETRICIDADE',
                                         'GÁS', 'CANALIZAÇÃO', 'TELECOMUNICAÇÕES', 'TERMICO', 'ACUSTICO', 'TRANSPORTES',
                                         'DIRETOR'))
);

create table PROFILE_PICTURE
(
    id      serial,
    user_id integer,
    name    varchar(255) not null,
    type    varchar(255) not null,
    file    bytea        not null,
    primary key (id),
    constraint UserId foreign key (user_id) references UTILIZADOR (id)
);

create table IMAGEM_OBRA
(
    id      serial,
    work_id varchar(255),
    name    varchar(255) not null,
    type    varchar(255) not null,
    file    bytea        not null,
    primary key (id),
    constraint WorkId foreign key (work_id) references OBRA (id)
);
