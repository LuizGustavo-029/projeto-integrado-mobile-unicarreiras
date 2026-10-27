# UniCarreiras (EstagioPro)

Aplicativo Android que conecta estudantes a vagas de estágio. O app se comunica com um backend em PHP, que acessa um banco de dados MySQL.

> Projeto Integrado Mobile — trabalho em grupo.

## Sobre o projeto

O UniCarreiras reúne estudantes e empresas em um só lugar: o estudante cria seu perfil, busca vagas e acompanha suas candidaturas, enquanto as empresas publicam vagas e avaliam os candidatos.

**Situação atual:** telas de **login** e **cadastro de estudante** (em duas etapas) integradas ao backend PHP e ao MySQL.

O banco de dados já modela o sistema completo: estudantes, empresas, administradores, vagas, candidaturas, habilidades, experiências, cursos complementares e notificações.

## Equipe

| Nome | Função |
| --- | --- |
| Michael de Jesus | Gerente de projetos |
| Luiz Gustavo | Líder técnico |
| Daniel Avelino | Desenvolvedor |
| Rafael Gomes | Desenvolvedor |
| Davi Fernandes | Desenvolvedor |
| Marcos Henrique | Desenvolvedor |
| Alexandre Viana | Desenvolvedor |
| Miguel Teixeira | Desenvolvedor |
| Gabriel Teixeira | Desenvolvedor |

## Tecnologias

- **App:** Android (Java), Retrofit 2 + Gson, Material Components
- **Backend:** PHP com PDO
- **Banco de dados:** MySQL (script em `database/`)
- **Versionamento:** Git e GitHub, com fluxo de branches `main` / `develop` / `feature/*`

Configuração do app: `minSdk 24`, `compileSdk 34`, Gradle 8.2.

## Estrutura do repositório

```
projeto-integrado-mobile-unicarreiras/
├── app/                  # Código-fonte do app Android
├── gradle/               # Gradle Wrapper
├── backend/              # API em PHP (login.php, cadastro.php)
├── database/             # Script SQL do banco (bd_unicarreiras.sql)
├── docs/                 # Documentação (wireframes, casos de uso, diagramas)
├── assets/               # Materiais de apoio (logos, imagens, artes)
├── tests/                # Testes
├── .github/              # Templates e automações do GitHub
├── build.gradle
├── settings.gradle
└── README.md
```

## Como rodar o projeto

### Pré-requisitos

- [Android Studio](https://developer.android.com/studio) (usa o JDK que já vem com ele)
- [XAMPP](https://www.apachefriends.org/) (Apache + MySQL) ou ambiente equivalente
- Git

### 1. Clonar o repositório

```bash
git clone https://github.com/LuizGustavo-029/projeto-integrado-mobile-unicarreiras.git
cd projeto-integrado-mobile-unicarreiras
git checkout develop
```

### 2. Configurar o banco de dados

1. Inicie o **Apache** e o **MySQL** no painel do XAMPP.
2. Abra o phpMyAdmin (ou outro cliente MySQL) e importe o arquivo `database/bd_unicarreiras.sql`.
3. O script cria o banco `db_unicarreiras`, as tabelas e alguns dados de exemplo.

### 3. Configurar o backend (PHP)

1. Copie o conteúdo da pasta `backend/` para `htdocs/unicarreiras/` do XAMPP, de modo que existam os endereços:
   - `http://localhost/unicarreiras/login.php`
   - `http://localhost/unicarreiras/cadastro.php`
2. Nos arquivos `login.php` e `cadastro.php`, a conexão com o MySQL está configurada assim:

   ```php
   new PDO("mysql:host=localhost;port=3307;dbname=db_unicarreiras;charset=utf8mb4", "root", "")
   ```

   **Atenção à porta:** o projeto usa `3307`, mas a porta padrão do MySQL no XAMPP é `3306`. Ajuste o número nos dois arquivos se o seu MySQL usar outra porta, e altere usuário e senha caso sejam diferentes.

### 4. Rodar o app Android

1. No Android Studio, abra a **raiz do repositório** (**File → Open**) e aguarde a sincronização do Gradle.
2. O app acessa o backend por `http://10.0.2.2/unicarreiras/` (definido em `app/src/main/java/com/example/estagiopro/api/RetrofitClient.java`). Esse endereço funciona no **emulador** Android e aponta para o `localhost` do computador.
3. Para testar em um **celular físico**, troque `10.0.2.2` pelo IP do computador na rede local (ex.: `http://192.168.0.10/unicarreiras/`), com celular e computador na mesma rede Wi-Fi.
4. Execute o app (**Run ▶**).

## Fluxo de trabalho (Git Flow simplificado)

- `main`: versão estável, só recebe código testado e aprovado.
- `develop`: integração das funcionalidades.
- `feature/nome-da-tarefa` e `docs/nome-da-tarefa`: branches de trabalho, criadas a partir da `develop`.

Todo código entra por **Pull Request**, com aprovação de pelo menos 1 outro integrante.

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nome-da-tarefa
# ... alterações ...
git add .
git commit -m "feat: descrição curta do que foi feito"
git push -u origin feature/nome-da-tarefa
# abrir o Pull Request para a develop no GitHub
```

## Documentação

Wireframes, casos de uso, diagramas e regras de negócio ficam na pasta [`docs/`](docs/).
