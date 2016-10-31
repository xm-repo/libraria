package libraria.document.attributes;

public enum Attributes {

    BASIC_FILEPATH("Каталог регистрации"),

    BASIC_FILENAME("Имя файла"),
    CONTENT_TYPE("Тип файла"),

    OS_SIZE("Размер (Байт)"),

    OS_CREATIONTIME("Атрибут файла \"Дата и время создания\""),
    OS_CREATIONTIME_Z("Атрибут файла \"Дата и время создания\" (UTC)"),

    OS_LASTACCESSTTIME("Атрибут файла \"Дата и время последнего доступа\""),
    OS_LASTACCESSTTIME_Z("Атрибут файла \"Дата и время последнего доступа\" (UTC)"),

    OS_LASTMODIFIEDTIME("Атрибут файла \"Дата и время последнего изменения\""),
    OS_LASTMODIFIEDTIME_Z("Атрибут файла \"Дата и время последнего изменения\" (UTC)"),

    EXTRA_CREATOR("Автор"),

    EXTRA_CREATED("Время и дата создания содержимого"),
    EXTRA_CREATED_Z("Время и дата создания содержимого (UTC)"),

    EXTRA_MODIFIED("Время и дата последнего сохранения"),
    EXTRA_MODIFIED_Z("Время и дата последнего сохранения (UTC)"),

    EXTRA_LASTPRINTED("Время и дата печати"),
    EXTRA_LASTPRINTED_Z("Время и дата печати (UTC)");

    Attributes(String title) {
        this.title = title;
    }

    public final String title;
}
