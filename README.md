# PESCD System

Repositório do sistema PESCD: [lucasmd30/pescd-system](https://github.com/lucasmd30/pescd-system/)

## Executando (infraestrutura)

O sistema depende de um PostgreSQL e de um SeaweedFS (armazenamento dos PDFs).
Ambos sobem via Docker Compose:

```bash
docker compose up -d      # sobe postgres (5432) e seaweedfs (9333/8080/8888)
./mvnw spring-boot:run    # sobe a API Spring Boot em http://localhost:8080
```

### Front-end React

O sistema agora tem um front-end em React (Vite) que consome a REST API,
substituindo as telas Thymeleaf. Veja [`frontend/README.md`](frontend/README.md):

```bash
cd frontend && npm install && npm run dev   # http://localhost:5173
```

### Armazenamento de arquivos no SeaweedFS

Os arquivos PDF (plano de trabalho, documentação e relatório) **não são mais
gravados como binário (`bytea`) no PostgreSQL**. Eles passam a ser enviados ao
[SeaweedFS](https://github.com/seaweedfs/seaweedfs#quick-start); no banco
relacional guardamos apenas o identificador retornado (o *fid*, ex.:
`3,01637037d6`) na coluna `file_fid` das tabelas `work_plans`, `documentations`
e `reports`. O acesso é encapsulado em `SeaweedFsStorageService`
(`/dir/assign` → upload no volume → `/dir/lookup` → leitura).

> **Migração de banco existente:** como o `spring.jpa.hibernate.ddl-auto=update`
> apenas *adiciona* a coluna `file_fid` e não remove a antiga `file_content`
> (que era `NOT NULL`), um banco já populado antes desta mudança precisa
> descartar a coluna antiga (ou ser recriado). Em bancos de desenvolvimento,
> basta recriar o schema; alternativamente:
>
> ```sql
> ALTER TABLE work_plans     DROP COLUMN file_content;
> ALTER TABLE documentations DROP COLUMN file_content;
> ALTER TABLE reports        DROP COLUMN file_content;
> ```

## Integrantes e contribuições

### Lucas Martinez

**Contribuições / user stories**

- Infraestrutura base do sistema
- `U.01` Login com controle por perfil
- `V.01` Visualização pública de ofertas
- `AD.01` CRUD administrativo de usuários
- Setup geral com Spring Security, entidades JPA, seed de dados, layout inicial em Thymeleaf, login/logout e tratamento inicial de erros

**Commits associados**

- [a48ebdb - Initial project setup with Spring Boot](https://github.com/lucasmd30/pescd-system/commit/a48ebdba4ac2512c693b10d59746e446d70b54aa)
- [7644c16 - feat: setup core infrastructure with auth and database config](https://github.com/lucasmd30/pescd-system/commit/7644c167aefd8422f65e84fa047a0ce644e52773)
- [c1a46ff - feat: login redirect](https://github.com/lucasmd30/pescd-system/commit/c1a46ff64d1806987799742288776530901d67b5)

### Felipe Cruzato Yacobian

**Contribuições / user stories**

- `S.03` Acompanhamento de ofertas
- `S.04` Encerramento de ofertas
- `PR.04` Acompanhamento de ofertas pelo professor responsável
- Encerramento de oferta pelo professor responsável
- Busca de alunos por nome na oferta
- Correções e ajustes complementares

**Commits associados**

- [412d37a - Implementa S.03 acompanhamento de ofertas](https://github.com/lucasmd30/pescd-system/commit/412d37a)
- [8670dfe - Implementa S.04 encerramento de ofertas](https://github.com/lucasmd30/pescd-system/commit/8670dfe)
- [2e78216 - feat: correções](https://github.com/lucasmd30/pescd-system/commit/2e78216)
- [4b6bd17 - feat: implementa encerramento de oferta pelo professor responsável](https://github.com/lucasmd30/pescd-system/commit/4b6bd17)
- [5a7fa17 - Implementa PR.04 - acompanhamento de ofertas pelo professor responsável](https://github.com/lucasmd30/pescd-system/commit/5a7fa17)
- [e254eca - feat: adiciona busca de alunos por nome na oferta](https://github.com/lucasmd30/pescd-system/commit/e254eca)

### Heitor Giometti

**Contribuições / user stories**

- `PS.01` Visualização, pelo professor supervisor, das ofertas e alunos inscritos
- `PS.02` Aprovação de plano de trabalho do aluno
- `PS.03` Aprovação de relatório de estágio com parecer, indicador de frequência e sugestão de nota
- `PR.01` Conclusão do relatório de estágio de um aluno como professor responsável
- `PR.02` Análise e aprovação de documentação de aula enviada por um aluno

**Commits associados**

- [38b17ac - feature/PS01: implementação de visualização de professor supervisor das ofertas e alunos inscritos](https://github.com/lucasmd30/pescd-system/commit/38b17ac)
- [5eb62ad - feature/PS02: implementação de funcionalidade para aprovacao de plano de trabalho para o aluno](https://github.com/lucasmd30/pescd-system/commit/5eb62ad)
- [1bf23fc - feature/PS03: implementação de aprovacao de relatorio de estagio para da andamento a finalização do estágio com parecer indicador de frequencia e sugestao de nota](https://github.com/lucasmd30/pescd-system/commit/1bf23fc)
- [bf883a3 - feature/PR01: Implementação da funcionalidade de conclusão do relatório do estágio de um aluno como professor responsável](https://github.com/lucasmd30/pescd-system/commit/bf883a3)
- [d99ff49 - feature/PR02: Implementação da funcionalidade de analise e aprovação de documentação de aula enviada por um aluno](https://github.com/lucasmd30/pescd-system/commit/d99ff49)

### Daniel Gidrão

**Contribuições / user stories**

- `S.01` e `S.02`
- `AL.01`, `AL.02`, `AL.03` e `AL.04`
- Fluxo do aluno
- Internacionalização do sistema

**Commits associados**

- [01f3803 - Implementa S.01, S.02 e AL.01-AL.04 (fluxo do aluno)](https://github.com/lucasmd30/pescd-system/commit/01f3803)
- [5e99e58 - Implementa internacionalização](https://github.com/lucasmd30/pescd-system/commit/5e99e58)

