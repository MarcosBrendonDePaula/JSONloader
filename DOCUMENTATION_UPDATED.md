# Documentação Atualizada do JSONloader

## Visão Geral

O JSONloader é um mod para Minecraft Forge que permite adicionar novos blocos, itens e drops ao jogo usando apenas arquivos JSON, sem precisar escrever código Java. Esta abordagem torna a criação de conteúdo para Minecraft muito mais acessível para não-programadores.

## Funcionalidades

- Carregamento de múltiplos mods a partir de arquivos JSON
- Suporte para blocos personalizados com propriedades customizáveis
- Suporte para itens personalizados, incluindo ferramentas e comida
- Sistema de drops para blocos e mobs
- Suporte para abas criativas personalizadas para cada mod
- Texturas embutidas em Base64 ou referenciadas por arquivo
- Comando `/jsonmods` para listar e gerenciar mods carregados

## Instalação

1. Instale o Minecraft Forge para a versão apropriada
2. Coloque o arquivo JAR do JSONloader na pasta `mods`
3. Crie uma pasta `jsonmods` no diretório principal do jogo
4. Adicione seus mods JSON em subpastas dentro de `jsonmods`
5. Inicie o jogo

## Estrutura de Pastas

```
minecraft/
├── mods/
│   └── jsonloader-1.0.0.jar
├── jsonmods/
│   ├── mod_gemas/
│   │   ├── mod.json
│   │   ├── blocks.json
│   │   ├── items.json
│   │   └── drops.json
│   ├── mod_espadas/
│   │   ├── mod.json
│   │   └── items.json
│   └── mod_metais/
│       ├── mod.json
│       ├── blocks.json
│       └── items.json
```

## Formato do Arquivo mod.json

Cada mod deve ter um arquivo `mod.json` na raiz de sua pasta, contendo metadados e configurações:

```json
{
  "mod_id": "mod_exemplo",
  "name": "Mod de Exemplo",
  "version": "1.0.0",
  "description": "Um mod de exemplo para demonstrar o JSONloader",
  "author": "Seu Nome",
  "website": "https://exemplo.com",
  "dependencies": [
    {
      "mod_id": "outro_mod",
      "version_required": "1.0.0"
    }
  ],
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

- `mod_id`: Identificador único do mod (sem espaços, apenas letras, números e underscores)
- `name`: Nome de exibição do mod
- `version`: Versão do mod
- `description`: Descrição breve do mod

### Campos Opcionais

- `author`: Autor do mod
- `website`: Site do mod
- `dependencies`: Lista de mods dos quais este mod depende
- `assets`: Configuração de arquivos de recursos
- `creative_tabs`: Lista de abas criativas personalizadas para o mod

## Abas Criativas Personalizadas

Cada mod pode definir suas próprias abas criativas para organizar seus itens e blocos:

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

### Campos da Aba Criativa

- `id`: Identificador único da aba (sem espaços)
- `title`: Título de exibição da aba
- `icon_item`: Item a ser usado como ícone da aba (formato: "mod_id_item_id")
- `background_texture`: Textura de fundo personalizada (opcional)
- `search_tags`: Tags para pesquisa rápida
- `show_search_bar`: Se deve mostrar barra de pesquisa
- `position_before`: ID da aba vanilla antes da qual esta aba deve aparecer
- `position_after`: ID da aba vanilla após a qual esta aba deve aparecer

Nota: Use apenas um dos campos `position_before` ou `position_after`, não ambos.

## Comando /jsonmods

O JSONloader inclui um comando para listar e gerenciar mods carregados:

- `/jsonmods` ou `/jsonmods list` - Lista todos os mods carregados
- `/jsonmods info <mod_id>` - Mostra informações detalhadas sobre um mod específico
- `/jsonmods count` - Exibe estatísticas dos mods carregados
- `/jsonmods reload` - Recarrega todos os mods (apenas para operadores)

## Exemplos

Veja a pasta `examples/` para exemplos completos de mods JSON.

## Solução de Problemas

Se encontrar problemas ao carregar mods:

1. Verifique o arquivo de log para mensagens de erro detalhadas
2. Use o comando `/jsonmods` para verificar quais mods foram carregados
3. Certifique-se de que o arquivo `mod.json` está corretamente formatado
4. Verifique se os IDs de itens e blocos são únicos em todos os mods

## Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests no repositório GitHub.
