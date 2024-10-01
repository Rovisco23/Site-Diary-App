delete from TERMO_ABERTURA;
delete from DOCUMENTO;
delete from IMAGEM;
delete from REGISTO;
delete from MEMBRO;
delete from OBRA;
delete from SESSAO;
delete from UTILIZADOR;
delete from LOCALIDADE;

insert into utilizador(email, role, username, password, nome, apelido, nif, telefone, freguesia, concelho, distrito,
                       associacao_nome, associacao_numero, pendente)
values ('a49508@alunos.isel.pt', 'CÂMARA', 'JMota15', 't1S51egvB9silDAQyyIfT_gNbeJiVT_vkfpC9bsbPMg=', 'João', 'Mota', 266573150, '910937960',
        'Marvila', 'Lisboa', 'Lisboa', 'Site Diary Team', 1, false),
       ('ricardorovisco23@gmail.com', 'ADMIN', 'Rovisco23', 'vhlmivklL3Ng_bdUoEqogay-yHVIZFFWJiFFWWtOs1g=', 'Ricardo', 'Rovisco', 234565432, NULL,
        'Santa Clara', 'Lisboa', 'Lisboa', 'Site Diary Team', 2, false);

-- Alterar local do ficheiro conforme necessário
COPY LOCALIDADE (distrito, concelho, freguesia) FROM 'E:\Documentos\Universidade\Projeto\PS-23-24\web-app\jvm\src\main\SQL\localidades.csv' DELIMITER ';' CSV HEADER;

