# README - JSONloader

## Visão Geral
JSONloader é um modloader simples para Minecraft Forge 1.20.1 que permite carregar blocos e itens dinamicamente a partir de ficheiros JSON, sem necessidade de programar em Java.

## Funcionalidades Principais

### Carregamento de Blocos
- Defina blocos personalizados usando um formato JSON simples
- Suporte para texturas locais, URLs remotas ou imagens codificadas em Base64
- Configure propriedades como dureza, resistência e requisitos de ferramentas

### Carregamento de Itens (Novo!)
- Defina itens personalizados usando JSON
- Suporte para diferentes tipos de itens:
  - Itens básicos
  - Comidas com efeitos personalizados
  - Ferramentas (espadas, picaretas, machados, pás, enxadas)
- Configure propriedades como tamanho da pilha, durabilidade e atributos específicos

### Configuração de Drops (Novo!)
- Configure drops personalizados para blocos e mobs
- Defina condições específicas para drops:
  - Requisitos de ferramentas
  - Compatibilidade com encantamentos (Fortune, Silk Touch, Looting)
  - Probabilidades e quantidades variáveis

## Como Usar

### Instalação
1. Baixe o ficheiro JAR do JSONloader
2. Coloque-o na pasta `mods` do seu Minecraft Forge 1.20.1
3. Inicie o jogo para gerar os ficheiros de configuração

### Configuração de Blocos
Edite o ficheiro `assets/jsonloader/blocks.json`:

```json
[
  {
    "id": "magic_ore",
    "name": "Minério Mágico",
    "material": "stone",
    "properties": {
      "hardness": 3.0,
      "resistance": 3.0,
      "requires_tool": true
    },
    "texture": {
      "type": "url",
      "value": "https://exemplo.com/textura.png"
    }
  }
]
```

### Configuração de Itens (Novo!)
Edite o ficheiro `assets/jsonloader/items.json`:

```json
[
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
  },
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
]
```

### Configuração de Drops (Novo!)
Edite o ficheiro `assets/jsonloader/drops.json`:

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
        }
      ]
    }
  ],
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

## Versionamento (Novo!)
O JSONloader segue o padrão de Versionamento Semântico 2.0.0, utilizando o formato MAJOR.MINOR.PATCH. Consulte o ficheiro VERSIONING.md para mais detalhes.

## Contribuição
Contribuições são bem-vindas! Consulte o ficheiro CONTRIBUTING.md para diretrizes.

## Licença
Este projeto está licenciado sob a licença MIT - veja o ficheiro LICENSE para detalhes.

## Links
- [GitHub](https://github.com/MarcosBrendonDePaula/JSONloader)
- [Site](https://mlyxhwtb.manus.space)
