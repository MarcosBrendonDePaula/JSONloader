# Definição do Formato JSON para Itens

Este documento descreve o formato JSON utilizado pelo JSONloader para definir itens personalizados.

## Estrutura Básica

```json
[
  {
    "id": "string",
    "name": "string",
    "type": "string",
    "properties": {
      "max_stack_size": number,
      "durability": number,
      "food_properties": {
        "nutrition": number,
        "saturation_modifier": number,
        "is_meat": boolean,
        "can_always_eat": boolean,
        "effects": [
          {
            "effect_id": "string",
            "duration": number,
            "amplifier": number,
            "probability": number
          }
        ]
      },
      "tool_properties": {
        "tier": "string",
        "attack_damage_modifier": number,
        "attack_speed_modifier": number,
        "efficiency": number
      }
    },
    "texture": {
      "type": "string",
      "value": "string"
    }
  }
]
```

## Campos Detalhados

### Campos Principais
- `id`: Identificador único do item (sem espaços, apenas letras minúsculas, números e underscores)
- `name`: Nome de exibição do item (pode conter espaços e caracteres especiais)
- `type`: Tipo do item (valores possíveis: "basic", "food", "tool_sword", "tool_pickaxe", "tool_axe", "tool_shovel", "tool_hoe")

### Propriedades
- `max_stack_size`: Tamanho máximo da pilha (padrão: 64)
- `durability`: Durabilidade para ferramentas (ignorado para itens básicos)

### Propriedades de Comida (apenas para type: "food")
- `nutrition`: Quantidade de pontos de fome restaurados
- `saturation_modifier`: Modificador de saturação
- `is_meat`: Se o item é considerado carne (afeta efeitos de poções e comportamentos)
- `can_always_eat`: Se o item pode ser consumido mesmo com a barra de fome cheia
- `effects`: Lista de efeitos de status aplicados ao consumir o item
  - `effect_id`: ID do efeito (ex: "minecraft:speed", "minecraft:strength")
  - `duration`: Duração em ticks (20 ticks = 1 segundo)
  - `amplifier`: Nível do efeito (0 = nível I, 1 = nível II, etc.)
  - `probability`: Probabilidade de aplicar o efeito (0.0 a 1.0)

### Propriedades de Ferramenta (apenas para tipos de ferramenta)
- `tier`: Nível do material ("wood", "stone", "iron", "diamond", "gold", "netherite")
- `attack_damage_modifier`: Modificador de dano de ataque
- `attack_speed_modifier`: Modificador de velocidade de ataque
- `efficiency`: Velocidade de mineração (apenas para ferramentas)

### Textura
- `type`: Tipo de textura ("local", "url", "base64")
- `value`: Valor da textura
  - Para "local": nome do arquivo na pasta de recursos
  - Para "url": URL completa da imagem
  - Para "base64": string Base64 da imagem (com ou sem prefixo data:image)

## Exemplos

### Item Básico
```json
{
  "id": "magic_dust",
  "name": "Pó Mágico",
  "type": "basic",
  "properties": {
    "max_stack_size": 64
  },
  "texture": {
    "type": "local",
    "value": "magic_dust"
  }
}
```

### Item de Comida
```json
{
  "id": "enchanted_apple",
  "name": "Maçã Encantada",
  "type": "food",
  "properties": {
    "max_stack_size": 16,
    "food_properties": {
      "nutrition": 4,
      "saturation_modifier": 1.2,
      "is_meat": false,
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
    "type": "url",
    "value": "https://exemplo.com/textura_maca.png"
  }
}
```

### Ferramenta (Espada)
```json
{
  "id": "frost_sword",
  "name": "Espada de Gelo",
  "type": "tool_sword",
  "properties": {
    "durability": 1500,
    "tool_properties": {
      "tier": "diamond",
      "attack_damage_modifier": 3.0,
      "attack_speed_modifier": -2.4
    }
  },
  "texture": {
    "type": "base64",
    "value": "data:image/png;base64,..."
  }
}
```
