# JSONloader - Documentação

O JSONloader é um mod para Minecraft Forge 1.20.1 que permite carregar blocos, itens e outros recursos dinamicamente a partir de arquivos JSON, sem a necessidade de programação Java.

## Índice

1. [Visão Geral](#visão-geral)
2. [Instalação](#instalação)
3. [Estrutura de Mods JSON](#estrutura-de-mods-json)
4. [Arquivo mod.json](#arquivo-modjson)
5. [Definição de Blocos](#definição-de-blocos)
6. [Definição de Itens](#definição-de-itens)
7. [Definição de Drops](#definição-de-drops)
8. [Creative Tabs Personalizadas](#creative-tabs-personalizadas)
9. [Sistema de Texturas Dinâmicas](#sistema-de-texturas-dinâmicas)
10. [Comandos](#comandos)
11. [Exemplos](#exemplos)
12. [Solução de Problemas](#solução-de-problemas)

## Visão Geral

O JSONloader permite que você crie novos blocos, itens e outros recursos para Minecraft sem precisar escrever código Java. Tudo é definido em arquivos JSON simples que são carregados pelo mod durante a inicialização do jogo.

Principais recursos:
- Carregamento de múltiplos mods a partir da pasta `jsonmods`
- Suporte para blocos e itens personalizados
- Suporte para texturas remotas (URL) ou embutidas (Base64)
- Sistema de drops personalizados
- Creative tabs personalizadas
- Comandos para listar e gerenciar mods carregados

## Instalação

1. Instale o Minecraft Forge 1.20.1
2. Coloque o arquivo JAR do JSONloader na pasta `mods`
3. Crie uma pasta chamada `jsonmods` no diretório principal do jogo
4. Adicione seus mods JSON em subpastas dentro de `jsonmods`
5. Inicie o jogo

## Estrutura de Mods JSON

Cada mod JSON deve estar em sua própria subpasta dentro da pasta `jsonmods`. A estrutura básica é:

```
jsonmods/
  ├── mod_exemplo1/
  │   ├── mod.json
  │   ├── blocks.json
  │   ├── items.json
  │   └── drops.json
  ├── mod_exemplo2/
  │   ├── mod.json
  │   ├── blocks.json
  │   └── items.json
  └── ...
```

## Arquivo mod.json

O arquivo `mod.json` contém os metadados do mod e é obrigatório para cada mod JSON. Exemplo:

```json
{
  "mod_id": "mod_exemplo",
  "name": "Mod de Exemplo",
  "version": "1.0.0",
  "description": "Um mod de exemplo para o JSONloader",
  "author": "Seu Nome",
  "website": "https://exemplo.com",
  "dependencies": [],
  "assets": {
    "blocks_file": "blocks.json",
    "items_file": "items.json",
    "drops_file": "drops.json"
  },
  "creative_tabs": [
    {
      "id": "exemplo_tab",
      "title": "Itens de Exemplo",
      "icon_item": "mod_exemplo_item_especial",
      "background_texture": "",
      "search_tags": ["exemplo", "demonstração"],
      "show_search_bar": true,
      "position_before": "minecraft:building_blocks",
      "position_after": ""
    }
  ]
}
```

### Campos Obrigatórios

- `mod_id`: Identificador único do mod (sem espaços, apenas letras minúsculas, números e underscores)
- `name`: Nome visível do mod
- `version`: Versão do mod (semântica recomendada: MAJOR.MINOR.PATCH)
- `description`: Descrição breve do mod

### Campos Opcionais

- `author`: Nome do autor do mod
- `website`: Site do mod
- `dependencies`: Lista de IDs de mods dos quais este mod depende
- `assets`: Configuração de arquivos de recursos
  - `blocks_file`: Nome do arquivo de blocos (padrão: "blocks.json")
  - `items_file`: Nome do arquivo de itens (padrão: "items.json")
  - `drops_file`: Nome do arquivo de drops (padrão: "drops.json")
- `creative_tabs`: Lista de abas criativas personalizadas (ver seção [Creative Tabs Personalizadas](#creative-tabs-personalizadas))

## Definição de Blocos

Os blocos são definidos no arquivo `blocks.json` (ou no arquivo especificado em `assets.blocks_file`). Exemplo:

```json
[
  {
    "id": "exemplo_bloco",
    "name": "Bloco de Exemplo",
    "material": "stone",
    "properties": {
      "hardness": 3.0,
      "resistance": 5.0,
      "requires_tool": true,
      "light_level": 0
    },
    "texture": {
      "type": "base64",
      "value": "iVBORw0KGgoAAAANSUhEUgAAA..."
    }
  }
]
```

### Campos Obrigatórios

- `id`: Identificador único do bloco (sem espaços, apenas letras minúsculas, números e underscores)
- `name`: Nome visível do bloco
- `material`: Material do bloco (stone, wood, metal, etc.)
- `texture`: Informações da textura do bloco
  - `type`: Tipo da textura (local, url, base64)
  - `value`: Valor da textura (caminho local, URL ou string Base64)

### Campos Opcionais

- `properties`: Propriedades do bloco
  - `hardness`: Dureza do bloco (padrão: 1.0)
  - `resistance`: Resistência à explosão (padrão: 1.0)
  - `requires_tool`: Se o bloco requer ferramenta para ser quebrado (padrão: false)
  - `light_level`: Nível de luz emitido pelo bloco (0-15, padrão: 0)
  - `sound_type`: Tipo de som do bloco (stone, wood, metal, etc., padrão: stone)

## Definição de Itens

Os itens são definidos no arquivo `items.json` (ou no arquivo especificado em `assets.items_file`). Exemplo:

```json
[
  {
    "id": "exemplo_item",
    "name": "Item de Exemplo",
    "type": "basic",
    "properties": {
      "max_stack_size": 64,
      "rarity": "common"
    },
    "texture": {
      "type": "base64",
      "value": "iVBORw0KGgoAAAANSUhEUgAAA..."
    }
  },
  {
    "id": "exemplo_espada",
    "name": "Espada de Exemplo",
    "type": "tool_sword",
    "properties": {
      "max_stack_size": 1,
      "rarity": "uncommon"
    },
    "tool_properties": {
      "tier": "diamond",
      "attack_damage_bonus": 2.0,
      "attack_speed_bonus": -2.0,
      "durability_multiplier": 1.5
    },
    "texture": {
      "type": "base64",
      "value": "iVBORw0KGgoAAAANSUhEUgAAA..."
    }
  }
]
```

### Campos Obrigatórios

- `id`: Identificador único do item (sem espaços, apenas letras minúsculas, números e underscores)
- `name`: Nome visível do item
- `type`: Tipo do item (basic, tool_sword, tool_pickaxe, tool_axe, tool_shovel, tool_hoe, armor_helmet, armor_chestplate, armor_leggings, armor_boots)
- `texture`: Informações da textura do item
  - `type`: Tipo da textura (local, url, base64)
  - `value`: Valor da textura (caminho local, URL ou string Base64)

### Campos Opcionais

- `properties`: Propriedades gerais do item
  - `max_stack_size`: Tamanho máximo da pilha (padrão: 64)
  - `rarity`: Raridade do item (common, uncommon, rare, epic, padrão: common)
  - `fire_resistant`: Se o item é resistente ao fogo (padrão: false)
  - `glow`: Se o item brilha mesmo sem encantamentos (padrão: false)

- `tool_properties`: Propriedades específicas para itens do tipo ferramenta (obrigatório para tipos tool_*)
  - `tier`: Nível da ferramenta (wood, stone, iron, diamond, gold, netherite)
  - `attack_damage_bonus`: Bônus de dano de ataque (padrão: 0.0)
  - `attack_speed_bonus`: Bônus de velocidade de ataque (padrão: 0.0)
  - `durability_multiplier`: Multiplicador de durabilidade (padrão: 1.0)

- `armor_properties`: Propriedades específicas para itens do tipo armadura (obrigatório para tipos armor_*)
  - `material`: Material da armadura (leather, chainmail, iron, gold, diamond, netherite)
  - `defense_points`: Pontos de defesa (padrão: depende do tipo)
  - `toughness`: Resistência da armadura (padrão: depende do material)
  - `knockback_resistance`: Resistência a repulsão (padrão: 0.0)

## Definição de Drops

Os drops são definidos no arquivo `drops.json` (ou no arquivo especificado em `assets.drops_file`). Exemplo:

```json
{
  "block_drops": [
    {
      "block_id": "exemplo_bloco",
      "drops": [
        {
          "item_id": "exemplo_item",
          "count": {
            "min": 1,
            "max": 3
          },
          "conditions": {
            "requires_silk_touch": false,
            "min_fortune_level": 0
          }
        }
      ]
    }
  ],
  "entity_drops": [
    {
      "entity_id": "minecraft:zombie",
      "drops": [
        {
          "item_id": "exemplo_item",
          "count": {
            "min": 0,
            "max": 1
          },
          "chance": 0.25,
          "conditions": {
            "requires_player_kill": true,
            "min_looting_level": 0
          }
        }
      ]
    }
  ]
}
```

### Block Drops

- `block_id`: ID do bloco que terá drops personalizados
- `drops`: Lista de itens que podem ser dropados
  - `item_id`: ID do item a ser dropado
  - `count`: Quantidade a ser dropada
    - `min`: Quantidade mínima
    - `max`: Quantidade máxima
  - `conditions`: Condições para o drop
    - `requires_silk_touch`: Se requer Toque Suave (padrão: false)
    - `min_fortune_level`: Nível mínimo de Fortuna (padrão: 0)

### Entity Drops

- `entity_id`: ID da entidade que terá drops personalizados
- `drops`: Lista de itens que podem ser dropados
  - `item_id`: ID do item a ser dropado
  - `count`: Quantidade a ser dropada
    - `min`: Quantidade mínima
    - `max`: Quantidade máxima
  - `chance`: Chance de drop (0.0 a 1.0, padrão: 1.0)
  - `conditions`: Condições para o drop
    - `requires_player_kill`: Se requer morte por jogador (padrão: false)
    - `min_looting_level`: Nível mínimo de Pilhagem (padrão: 0)

## Creative Tabs Personalizadas

As abas criativas personalizadas são definidas no arquivo `mod.json` na seção `creative_tabs`. Exemplo:

```json
"creative_tabs": [
  {
    "id": "exemplo_tab",
    "title": "Itens de Exemplo",
    "icon_item": "mod_exemplo_item_especial",
    "background_texture": "",
    "search_tags": ["exemplo", "demonstração"],
    "show_search_bar": true,
    "position_before": "minecraft:building_blocks",
    "position_after": ""
  }
]
```

### Campos Obrigatórios

- `id`: Identificador único da aba (sem espaços, apenas letras minúsculas, números e underscores)
- `title`: Título visível da aba
- `icon_item`: ID do item que será usado como ícone da aba

### Campos Opcionais

- `background_texture`: Textura de fundo da aba (padrão: textura padrão do Minecraft)
- `search_tags`: Tags de pesquisa para a aba
- `show_search_bar`: Se a barra de pesquisa deve ser exibida (padrão: true)
- `position_before`: ID da aba antes da qual esta aba deve ser posicionada
- `position_after`: ID da aba após a qual esta aba deve ser posicionada

Observação: Você deve definir apenas um dos campos `position_before` ou `position_after`, não ambos.

## Sistema de Texturas Dinâmicas

O JSONloader suporta três tipos de texturas:

1. **Local**: Texturas armazenadas no resource pack do mod
2. **URL**: Texturas baixadas de URLs durante o carregamento do jogo
3. **Base64**: Texturas embutidas como strings Base64 no arquivo JSON

### Texturas Locais

```json
"texture": {
  "type": "local",
  "value": "nome_da_textura"
}
```

As texturas locais devem estar no resource pack do mod, seguindo a estrutura padrão do Minecraft:
- Blocos: `assets/jsonloader/textures/block/nome_da_textura.png`
- Itens: `assets/jsonloader/textures/item/nome_da_textura.png`

### Texturas URL

```json
"texture": {
  "type": "url",
  "value": "https://exemplo.com/textura.png"
}
```

As texturas URL são baixadas durante o carregamento do jogo e armazenadas em um resource pack dinâmico.

### Texturas Base64

```json
"texture": {
  "type": "base64",
  "value": "iVBORw0KGgoAAAANSUhEUgAAA..."
}
```

As texturas Base64 são decodificadas durante o carregamento do jogo e armazenadas em um resource pack dinâmico.

## Comandos

O JSONloader adiciona os seguintes comandos:

- `/jsonmods` ou `/jsonmods list`: Lista todos os mods carregados
- `/jsonmods info <mod_id>`: Mostra informações detalhadas sobre um mod específico
- `/jsonmods count`: Exibe estatísticas dos mods carregados (total de blocos, itens, drops)
- `/jsonmods reload`: Recarrega todos os mods (apenas para operadores)

## Exemplos

Veja a pasta `examples` para exemplos completos de mods JSON:

- `mod_gemas`: Adiciona 5 blocos de minérios de gemas e 5 itens de gemas
- `mod_espadas`: Adiciona 3 espadas únicas com propriedades diferentes
- `mod_metais`: Adiciona 3 minérios e 5 lingotes de metais

## Solução de Problemas

### Texturas não aparecem

Se as texturas não estiverem aparecendo corretamente:

1. Verifique se o formato da textura é PNG válido
2. Para texturas Base64, certifique-se de que a string está completa e corretamente codificada
3. Para texturas URL, verifique se a URL está acessível e retorna uma imagem PNG válida
4. Verifique os logs do jogo para mensagens de erro relacionadas ao carregamento de texturas

### Mods não carregam

Se os mods não estiverem carregando:

1. Verifique se a pasta `jsonmods` está no diretório correto
2. Verifique se cada mod tem um arquivo `mod.json` válido
3. Verifique se os IDs dos mods são únicos
4. Verifique os logs do jogo para mensagens de erro relacionadas ao carregamento de mods

### Erros de registro

Se houver erros durante o registro de blocos ou itens:

1. Verifique se os IDs dos blocos e itens são únicos
2. Verifique se os tipos de blocos e itens são válidos
3. Verifique se as propriedades obrigatórias estão definidas
4. Verifique os logs do jogo para mensagens de erro específicas
