# Formato do Arquivo mod.json

Este documento define o formato padrão para o arquivo `mod.json` que deve estar presente em cada pasta de mod dentro do diretório `jsonmods`.

## Estrutura Básica

```json
{
  "mod_id": "exemplo_mod",
  "name": "Exemplo de Mod",
  "version": "1.0.0",
  "description": "Este é um exemplo de mod para o JSONloader",
  "author": "Seu Nome",
  "website": "https://exemplo.com",
  "dependencies": [
    {
      "mod_id": "outro_mod",
      "version_required": ">=1.0.0"
    }
  ],
  "assets": {
    "blocks_file": "blocks.json",
    "items_file": "items.json",
    "drops_file": "drops.json"
  }
}
```

## Campos Obrigatórios

- **mod_id**: Identificador único do mod (apenas letras minúsculas, números e underscores)
- **name**: Nome de exibição do mod
- **version**: Versão do mod no formato semântico (MAJOR.MINOR.PATCH)
- **description**: Breve descrição do mod

## Campos Opcionais

- **author**: Nome do autor ou equipe que criou o mod
- **website**: Site ou repositório do mod
- **dependencies**: Lista de outros mods necessários para o funcionamento
  - **mod_id**: Identificador do mod dependente
  - **version_required**: Versão mínima necessária (usando operadores >=, >, =, <, <=)
- **assets**: Configuração dos arquivos de recursos
  - **blocks_file**: Nome do arquivo de definições de blocos (padrão: "blocks.json")
  - **items_file**: Nome do arquivo de definições de itens (padrão: "items.json")
  - **drops_file**: Nome do arquivo de definições de drops (padrão: "drops.json")

## Observações

- Se o campo `assets` não for especificado, serão usados os nomes padrão dos arquivos
- O `mod_id` será usado como prefixo para todos os recursos do mod, garantindo unicidade
- A validação de dependências ocorre durante o carregamento do mod
