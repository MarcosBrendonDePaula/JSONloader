# Propriedades de Itens no JSONloader

Este documento explica as propriedades disponíveis para definição de itens no JSONloader.

## Estrutura Básica

```json
{
  "id": "item_id",
  "name": "Nome do Item",
  "type": "basic",
  "properties": {
    "max_stack_size": 64,
    "rarity": "common",
    "durability": 0
  },
  "texture": {
    "type": "base64",
    "value": "..."
  }
}
```

## Tipos de Itens

O campo `type` define o tipo de item a ser criado:

- `basic`: Item básico sem funcionalidades especiais
- `food`: Item de comida com propriedades alimentares
- `tool_sword`: Espada
- `tool_pickaxe`: Picareta
- `tool_axe`: Machado
- `tool_shovel`: Pá
- `tool_hoe`: Enxada

## Propriedades Comuns

Estas propriedades são aplicáveis a todos os tipos de itens:

```json
"properties": {
  "max_stack_size": 64,
  "rarity": "common",
  "durability": 0
}
```

- `max_stack_size`: Tamanho máximo da pilha (1-64)
- `rarity`: Raridade do item (`common`, `uncommon`, `rare`, `epic`)
- `durability`: Durabilidade do item (0 para itens sem durabilidade)

## Propriedades de Ferramentas

**IMPORTANTE**: Para itens do tipo `tool_*`, é obrigatório incluir a seção `tool_properties`:

```json
"properties": {
  "max_stack_size": 1,
  "durability": 1500,
  "tool_properties": {
    "tier": "iron",
    "attack_damage_modifier": 2.0,
    "attack_speed_modifier": 0.0,
    "efficiency": 6.0,
    "enchantability": 14
  }
}
```

- `tier`: Nível da ferramenta (`wood`, `stone`, `iron`, `gold`, `diamond`, `netherite`)
- `attack_damage_modifier`: Modificador de dano adicional
- `attack_speed_modifier`: Modificador de velocidade de ataque
- `efficiency`: Eficiência da ferramenta (velocidade de mineração)
- `enchantability`: Facilidade de encantamento

## Propriedades de Comida

Para itens do tipo `food`, inclua a seção `food_properties`:

```json
"properties": {
  "max_stack_size": 64,
  "food_properties": {
    "nutrition": 4,
    "saturation_modifier": 0.3,
    "is_meat": false,
    "can_always_eat": false,
    "effects": [
      {
        "effect_id": "minecraft:regeneration",
        "duration": 100,
        "amplifier": 0,
        "probability": 1.0
      }
    ]
  }
}
```

- `nutrition`: Quantidade de pontos de fome restaurados
- `saturation_modifier`: Modificador de saturação
- `is_meat`: Se o item é considerado carne
- `can_always_eat`: Se o item pode ser comido mesmo com fome cheia
- `effects`: Lista de efeitos aplicados ao consumir o item

## Exemplos

### Item Básico

```json
{
  "id": "ruby",
  "name": "Rubi",
  "type": "basic",
  "properties": {
    "max_stack_size": 64,
    "rarity": "uncommon"
  },
  "texture": {
    "type": "base64",
    "value": "..."
  }
}
```

### Espada

```json
{
  "id": "ruby_sword",
  "name": "Espada de Rubi",
  "type": "tool_sword",
  "properties": {
    "max_stack_size": 1,
    "rarity": "rare",
    "durability": 1561,
    "tool_properties": {
      "tier": "diamond",
      "attack_damage_modifier": 3.0,
      "attack_speed_modifier": 0.0
    }
  },
  "texture": {
    "type": "base64",
    "value": "..."
  }
}
```

### Comida

```json
{
  "id": "magic_apple",
  "name": "Maçã Mágica",
  "type": "food",
  "properties": {
    "max_stack_size": 16,
    "food_properties": {
      "nutrition": 4,
      "saturation_modifier": 1.2,
      "can_always_eat": true,
      "effects": [
        {
          "effect_id": "minecraft:regeneration",
          "duration": 100,
          "amplifier": 1,
          "probability": 1.0
        }
      ]
    }
  },
  "texture": {
    "type": "base64",
    "value": "..."
  }
}
```
