# Definição do Formato JSON para Drops

Este documento descreve o formato JSON utilizado pelo JSONloader para definir drops de itens de blocos e mobs.

## Estrutura Básica

```json
{
  "block_drops": [
    {
      "block_id": "string",
      "drops": [
        {
          "item_id": "string",
          "count_min": number,
          "count_max": number,
          "chance": number,
          "conditions": {
            "requires_silk_touch": boolean,
            "requires_tool": boolean,
            "min_tool_tier": "string",
            "fortune_multiplier": number
          }
        }
      ]
    }
  ],
  "mob_drops": [
    {
      "mob_id": "string",
      "drops": [
        {
          "item_id": "string",
          "count_min": number,
          "count_max": number,
          "chance": number,
          "conditions": {
            "requires_player_kill": boolean,
            "requires_fire_aspect": boolean,
            "looting_multiplier": number
          }
        }
      ]
    }
  ]
}
```

## Campos Detalhados

### Drops de Blocos
- `block_id`: ID do bloco que irá dropar os itens (pode ser um bloco vanilla ou um bloco personalizado)
- `drops`: Lista de itens que podem ser dropados quando o bloco é quebrado
  - `item_id`: ID do item a ser dropado (pode ser um item vanilla ou um item personalizado)
  - `count_min`: Quantidade mínima do item a ser dropada
  - `count_max`: Quantidade máxima do item a ser dropada
  - `chance`: Probabilidade de o item ser dropado (0.0 a 1.0)
  - `conditions`: Condições especiais para o drop
    - `requires_silk_touch`: Se o drop requer encantamento Silk Touch
    - `requires_tool`: Se o drop requer uma ferramenta específica
    - `min_tool_tier`: Nível mínimo da ferramenta ("wood", "stone", "iron", "diamond", "netherite")
    - `fortune_multiplier`: Multiplicador para o encantamento Fortune (0 = sem efeito)

### Drops de Mobs
- `mob_id`: ID do mob que irá dropar os itens (ex: "minecraft:zombie", "minecraft:creeper")
- `drops`: Lista de itens que podem ser dropados quando o mob é morto
  - `item_id`: ID do item a ser dropado (pode ser um item vanilla ou um item personalizado)
  - `count_min`: Quantidade mínima do item a ser dropada
  - `count_max`: Quantidade máxima do item a ser dropada
  - `chance`: Probabilidade de o item ser dropado (0.0 a 1.0)
  - `conditions`: Condições especiais para o drop
    - `requires_player_kill`: Se o drop requer que o mob seja morto por um jogador
    - `requires_fire_aspect`: Se o drop requer que o mob seja morto com uma arma com encantamento Fire Aspect
    - `looting_multiplier`: Multiplicador para o encantamento Looting (0 = sem efeito)

## Exemplos

### Drops de Blocos
```json
{
  "block_drops": [
    {
      "block_id": "jsonloader:magic_ore",
      "drops": [
        {
          "item_id": "jsonloader:magic_dust",
          "count_min": 1,
          "count_max": 3,
          "chance": 1.0,
          "conditions": {
            "requires_silk_touch": false,
            "requires_tool": true,
            "min_tool_tier": "iron",
            "fortune_multiplier": 0.5
          }
        },
        {
          "item_id": "minecraft:diamond",
          "count_min": 1,
          "count_max": 1,
          "chance": 0.1,
          "conditions": {
            "requires_silk_touch": false,
            "requires_tool": true,
            "min_tool_tier": "diamond",
            "fortune_multiplier": 0.2
          }
        }
      ]
    }
  ]
}
```

### Drops de Mobs
```json
{
  "mob_drops": [
    {
      "mob_id": "minecraft:zombie",
      "drops": [
        {
          "item_id": "jsonloader:magic_dust",
          "count_min": 0,
          "count_max": 2,
          "chance": 0.25,
          "conditions": {
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
