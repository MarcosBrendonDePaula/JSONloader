# Documentação Atualizada do JSONloader

## Visão Geral
O JSONloader é um mod para Minecraft Forge 1.20.1 que permite adicionar novos blocos, itens e configurações de drops ao jogo através de arquivos JSON, sem necessidade de programação Java.

## Nova Estrutura de Mods
Cada mod é definido em uma subpasta dentro do diretório `jsonmods`. Cada subpasta deve conter:

1. **mod.json** - Arquivo principal com metadados e configurações do mod
2. **blocks.json** - Definições de blocos (opcional)
3. **items.json** - Definições de itens (opcional)
4. **drops.json** - Configurações de drops (opcional)

## Formato do Arquivo mod.json
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

### Campos Obrigatórios
- **mod_id**: Identificador único do mod (apenas letras minúsculas, números e underscores)
- **name**: Nome de exibição do mod
- **version**: Versão do mod no formato semântico (MAJOR.MINOR.PATCH)
- **description**: Breve descrição do mod

### Campos Opcionais
- **author**: Nome do autor ou equipe que criou o mod
- **website**: Site ou repositório do mod
- **dependencies**: Lista de outros mods necessários para o funcionamento
- **assets**: Configuração dos arquivos de recursos (nomes personalizados)

## Exemplos Incluídos
O JSONloader inclui vários exemplos de mods na pasta `examples/jsonmods_with_metadata`:

1. **mod_gemas**: 5 blocos de minérios e 5 itens de gemas
2. **mod_espadas**: 3 espadas únicas com propriedades diferentes
3. **mod_metais**: 3 minérios e 5 lingotes

## Como Usar
1. Instale o JSONloader no seu Minecraft Forge 1.20.1
2. Crie uma pasta `jsonmods` no diretório principal do jogo
3. Crie subpastas para cada mod que deseja adicionar
4. Adicione os arquivos necessários (mod.json, blocks.json, items.json, drops.json)
5. Inicie o jogo e os mods serão carregados automaticamente

## Formatos de Arquivos de Recursos
Para detalhes sobre os formatos específicos de cada arquivo de recursos, consulte:
- `block_format_definition.md` - Formato de definição de blocos
- `item_format_definition.md` - Formato de definição de itens
- `drops_format_definition.md` - Formato de configuração de drops
