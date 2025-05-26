# Documentação do JSONloader

## Visão Geral
O JSONloader é um modloader simples para Minecraft Forge 1.20.1 que permite criar mods completos usando apenas arquivos JSON, sem necessidade de programação em Java.

## Funcionalidades Principais
- Carregamento dinâmico de blocos, itens e drops a partir de arquivos JSON
- Suporte para texturas locais, URLs e Base64
- Carregamento de múltiplos mods a partir de uma pasta externa 'jsonmods'
- Sistema de drops personalizados para blocos e mobs
- Integração completa com o Minecraft Forge

## Como Usar

### Estrutura de Pastas
Para criar seus próprios mods, siga esta estrutura:
```
📁 jsonmods/
  📁 meu_mod_1/
    📄 blocks.json
    📄 items.json
    📄 drops.json
  📁 meu_mod_2/
    📄 blocks.json
    📄 items.json
    📄 drops.json
  ...
```

### Formato dos Arquivos

#### blocks.json
```json
[
  {
    "id": "meu_bloco",
    "name": "Meu Bloco",
    "material": "stone",
    "properties": {
      "hardness": 3.0,
      "resistance": 5.0,
      "requires_tool": true,
      "light_level": 0
    },
    "texture": {
      "type": "local",
      "value": "nome_da_textura"
    }
  }
]
```

Opções para texturas:
- `"type": "local"` - Usa uma textura local do mod (coloque em assets/jsonloader/textures/block/)
- `"type": "url"` - Baixa uma textura de uma URL
- `"type": "base64"` - Usa uma imagem codificada em Base64

#### items.json
```json
[
  {
    "id": "meu_item",
    "name": "Meu Item",
    "type": "basic",
    "properties": {
      "max_stack_size": 64
    },
    "texture": {
      "type": "local",
      "value": "nome_da_textura"
    }
  }
]
```

Tipos de itens suportados:
- `basic` - Item básico
- `food` - Comida com propriedades nutricionais e efeitos
- `tool_sword` - Espada
- `tool_pickaxe` - Picareta
- `tool_axe` - Machado
- `tool_shovel` - Pá
- `tool_hoe` - Enxada

#### drops.json
```json
{
  "block_drops": [
    {
      "block_id": "jsonloader:meu_bloco",
      "drops": [
        {
          "item_id": "jsonloader:meu_item",
          "count_min": 1,
          "count_max": 3,
          "chance": 0.5,
          "conditions": {
            "requires_silk_touch": false,
            "requires_tool": true,
            "min_tool_tier": "iron",
            "fortune_multiplier": 0.2,
            "requires_player_kill": false,
            "requires_fire_aspect": false,
            "looting_multiplier": 0.0
          }
        }
      ]
    }
  ],
  "mob_drops": [
    {
      "mob_id": "minecraft:zombie",
      "drops": [
        {
          "item_id": "jsonloader:meu_item",
          "count_min": 0,
          "count_max": 1,
          "chance": 0.1,
          "conditions": {
            "requires_silk_touch": false,
            "requires_tool": false,
            "min_tool_tier": "",
            "fortune_multiplier": 0.0,
            "requires_player_kill": true,
            "requires_fire_aspect": false,
            "looting_multiplier": 0.1
          }
        }
      ]
    }
  ]
}
```

## Instalação
1. Instale o Minecraft Forge 1.20.1
2. Coloque o arquivo JSONloader.jar na pasta 'mods'
3. Crie uma pasta 'jsonmods' no diretório principal do jogo
4. Adicione seus mods em subpastas dentro de 'jsonmods'
5. Inicie o jogo

## Exemplos
Veja os exemplos incluídos na pasta 'jsonmods' para entender melhor como criar seus próprios mods:
- mod_medieval: Blocos, itens e drops com tema medieval
- mod_futurista: Blocos, itens e drops com tema futurista
- mod_magico: Blocos, itens e drops com tema mágico
