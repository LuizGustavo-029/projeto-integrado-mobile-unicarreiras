# EstágioPro (Android · Java)

App de captação de estágio com três perfis: **Aluno**, **Empresa** e **Instituição de ensino**.
Funciona 100% offline: os dados ficam num banco SQLite local (criado e populado com dados de demonstração na primeira abertura).

## Como abrir
1. Android Studio (Hedgehog ou mais novo) → *Open* → pasta `EstagioPro`.
2. Aguarde o *Gradle Sync* (JDK 17, AGP 8.2.2, minSdk 26, compileSdk 34).
3. Rode em um emulador/dispositivo com Android 8.0+.

## Contas de demonstração (senha `12345678`)
| Perfil | E-mail |
|---|---|
| Aluno | ana@universidade.edu.br |
| Empresa | talentos@lumina.com.br |
| Instituição | carreiras@universidade.edu.br |

Também é possível criar contas novas nos três cadastros.

## O que funciona
- **Aluno:** início com vagas recomendadas (match por competências), busca e filtros, salvar vagas, candidatar/retirar candidatura, acompanhar etapas, perfil editável (competências, currículo em PDF/DOC, senha), notificações.
- **Empresa:** painel, publicar/editar vaga, rascunhos, pausar/reativar/encerrar/excluir, lista de candidatos com busca e filtro por vaga, favoritar, marcar em análise, agendar entrevista, contratar/recusar (o aluno é notificado).
- **Instituição:** painel, cadastrar oportunidades (revisão → publicada), alunos e egressos por situação, indicadores de empregabilidade, encaminhar aluno para vaga, alterar situação.
- Cadastro com validação (e-mail, CNPJ pelos dígitos verificadores, senha), login por portal, "Esqueci a senha".

## Limitações
- Dados somente no aparelho (sem servidor): contas e vagas não sincronizam entre dispositivos.
- Login com Google/Microsoft indisponível (sem backend); o botão avisa isso.
- "Esqueci a senha" redefine a senha localmente (não envia e-mail).

## Estrutura
`app/src/main/java/com/estagiopro/app` — telas (`*Activity`), `Ui` (helpers), `data/` (SQLite: `Db`, `Seeder`, `Repo`, modelos), `util/`, `widget/`.
`app/src/main/res/layout` — um XML por tela + `item_*` das listas.
