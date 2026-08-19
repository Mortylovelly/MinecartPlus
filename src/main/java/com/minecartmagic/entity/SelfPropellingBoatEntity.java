public double getMaximumSpeed() {

    /*
     * Сначала читаем настоящее зачарование
     * Попутный ветер с самой лодки.
     */
    int tailwindLevel =
            ModEnchantments.getTailwindLevel(
                    this
            );

    /*
     * Если attachment уже содержит уровень,
     * используем его напрямую.
     */
    if (tailwindLevel > 0) {

        return switch (tailwindLevel) {
            case 1 -> 0.62D;
            case 2 -> 0.72D;
            case 3 -> 0.84D;
            default -> BASE_ENGINE_SPEED;
        };
    }

    /*
     * Fallback для уже существующих лодок,
     * у которых уровень сохранён только
     * в ENGINE_TAILWIND_LEVEL.
     */
    int engineLevel =
            getEngineTailwindLevel();

    return switch (engineLevel) {
        case 1 -> 0.62D;
        case 2 -> 0.72D;
        case 3 -> 0.84D;
        default -> BASE_ENGINE_SPEED;
    };
}
