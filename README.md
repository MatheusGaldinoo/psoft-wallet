# P$oft Wallet

Esta é uma aplicação web que implementa um sistema de **Carteira de Investimentos** que permite aos usuários gerenciar seus ativos financeiros de forma inteligente e flexível.  

---

## Funcionalidades

- Cadastro e gerenciamento de ativos (Tesouro Direto, Ações, Criptomoedas)
- Controle de disponibilidade e atualização de cotações
- Cadastro de clientes com diferentes planos (Normal e Premium)
- Notificações sobre variações de ativos e disponibilidade
- Compras e resgates de ativos com cálculo automático de impostos
- Consulta detalhada de histórico de operações
- Exportação de extratos em formato CSV

---

## Estrutura do Sistema

- **Framework:** Spring Boot
- **Arquitetura:** Padrão Controller-Service-Repository
- **Pacotes:**
  - `controller`: expõe endpoints REST
  - `service`: lógica de negócio
  - `repository`: acesso a dados
  - `model`: entidades do sistema

  - `dtos`: Data Transfer Objects usados para comunicação entre camadas
  - `enums`: enumerações do sistema, como status, tipos, categorias
  - `exceptions`: exceções personalizadas
- **API REST:** Todas as funcionalidades disponíveis via endpoints

---

### 🔗 Endereços Úteis

- [Swagger](http://localhost:8080/swagger-ui/index.html)
- [H2 Console](http://localhost:8080/h2-console)
