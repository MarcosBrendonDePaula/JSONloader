# Versionamento do JSONloader

Este documento descreve a estratégia de versionamento semântico adotada pelo projeto JSONloader.

## Versionamento Semântico

O JSONloader segue o padrão de [Versionamento Semântico 2.0.0](https://semver.org/lang/pt-BR/), utilizando o formato MAJOR.MINOR.PATCH:

1. **MAJOR**: Incrementado quando há mudanças incompatíveis na API
2. **MINOR**: Incrementado quando há adição de funcionalidades compatíveis com versões anteriores
3. **PATCH**: Incrementado quando há correções de bugs compatíveis com versões anteriores

## Convenções de Branches

- **master**: Branch principal, contém código estável e pronto para produção
- **develop**: Branch de desenvolvimento, integra novas funcionalidades
- **feature/xxx**: Branches para desenvolvimento de novas funcionalidades
- **bugfix/xxx**: Branches para correção de bugs
- **release/x.y.z**: Branches temporárias para preparação de releases

## Convenções de Tags

As tags seguem o formato `v{MAJOR}.{MINOR}.{PATCH}`, por exemplo:
- `v1.0.0`: Primeira versão estável
- `v1.1.0`: Adição de novas funcionalidades
- `v1.1.1`: Correção de bugs

## Histórico de Versões

### v1.0.0 (Versão Inicial)
- Carregamento dinâmico de blocos via JSON
- Suporte para texturas locais, URL e Base64

### v1.1.0 (Versão Atual)
- Carregamento dinâmico de itens via JSON
- Configuração de drops de blocos e mobs
- Versionamento semântico e tags
- Testes automatizados no GitHub Actions

## Processo de Release

1. Criar branch `release/x.y.z` a partir de `develop`
2. Atualizar versão em `build.gradle` e `mods.toml`
3. Atualizar documentação e CHANGELOG
4. Merge da branch `release/x.y.z` para `master`
5. Criar tag `vx.y.z` no commit de merge
6. Merge da branch `release/x.y.z` para `develop`
