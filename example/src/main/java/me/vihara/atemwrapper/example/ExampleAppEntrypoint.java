package me.vihara.atemwrapper.example;

public class ExampleAppEntrypoint {
    public static void main(String[] args) {
        new ExampleApp();
        ExampleApp.LOGGER.info("Entrypoint passed successfully");
    }
}
