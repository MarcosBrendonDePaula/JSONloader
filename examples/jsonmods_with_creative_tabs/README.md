# Exemplo de Mod com Creative Tabs Personalizadas

Este diretório contém exemplos de mods JSON que utilizam o recurso de abas criativas personalizadas do JSONloader.

## Estrutura

Cada mod possui sua própria pasta com os seguintes arquivos:

- `mod.json` - Metadados do mod e configuração de abas criativas
- `blocks.json` - Definições de blocos (se aplicável)
- `items.json` - Definições de itens (se aplicável)
- `drops.json` - Definições de drops (se aplicável)

## Mods Incluídos

### mod_gemas
Um mod que adiciona minérios de gemas e itens de gemas preciosas, com uma aba criativa personalizada "Gemas Preciosas".

### mod_espadas
Um mod que adiciona espadas mágicas com propriedades especiais, com uma aba criativa personalizada "Espadas Mágicas".

### mod_metais
Um mod que adiciona novos minérios e lingotes de metais raros, com uma aba criativa personalizada "Metais Raros".

## Como Usar

1. Instale o JSONloader
2. Copie estas pastas para o diretório `jsonmods` no diretório principal do jogo
3. Inicie o jogo
4. As abas criativas personalizadas aparecerão no inventário criativo

## Personalização

Você pode personalizar as abas criativas editando a seção `creative_tabs` no arquivo `mod.json` de cada mod:

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

Consulte a documentação completa para mais detalhes sobre as opções disponíveis.
