# Contributing to TravelMaster Platform

Спасибо за интерес к проекту! Мы приветствуем ваш вклад.

## Как внести вклад

### Reporting Issues

Если вы обнаружили bug или у вас есть предложение:

1. Проверьте, нет ли уже открытого issue по вашей теме
2. Создайте новый issue с четким описанием:
   - Для bugs: steps to reproduce, expected vs actual behavior, environment details
   - Для features: clear use case, expected behavior, examples

### Pull Requests

1. **Fork** репозиторий
2. Создайте **feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit** ваши изменения (`git commit -m 'feat: add amazing feature'`)
4. **Push** в branch (`git push origin feature/amazing-feature`)
5. Откройте **Pull Request**

### Commit Messages

Используйте [Conventional Commits](https://www.conventionalcommits.org/):

- `feat: ` - новая функциональность
- `fix: ` - исправление бага
- `docs: ` - изменения в документации
- `style: ` - форматирование кода
- `refactor: ` - рефакторинг
- `test: ` - добавление тестов
- `chore: ` - обновление зависимостей, конфигурации

### Code Style

- Следуйте существующему code style проекта
- Используйте Java naming conventions
- Добавляйте JavaDoc для public methods
- Пишите unit tests для новой функциональности

### Testing

Перед созданием PR убедитесь что:

```bash
# Все тесты проходят
mvn clean test

# Проект собирается
mvn clean install

# Code coverage не снижен
mvn verify
```

### Code Review Process

1. Maintainer проверит ваш PR
2. Могут быть запрошены изменения
3. После approval PR будет merged

## Development Setup

```bash
# Clone repository
git clone https://github.com/shuldeshoff/travelmaster-platform.git
cd travelmaster-platform

# Install dependencies
mvn clean install

# Run tests
mvn test

# Start services
docker-compose up -d
```

## Questions?

- Telegram: [@shuldeshoff](https://t.me/shuldeshoff)
- GitHub Issues: [Create an issue](https://github.com/shuldeshoff/travelmaster-platform/issues)

Спасибо за ваш вклад! 🙏

