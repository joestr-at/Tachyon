package xyz.joestr.joecloud.provided.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import xyz.joestr.joecloud.command.ICommand;
import xyz.joestr.joecloud.command.ICommandSender;

public class PrintCommand implements ICommand {

    public PrintCommand() {
    }

    @Override
    public boolean callExecute(ICommandSender commandSender, String[] arguments) {

        if (arguments.length == 0) {

            return false;
        }

        commandSender.sendMessage(
            Arrays.asList(arguments).stream().collect(Collectors.joining(" "))
        );

        return true;
    }

    @Override
    public String usage() {

        return "print <Text ...>";
    }

    @Override
    public String getCommandString() {

        return "print";
    }

    @Override
    public String[] callCompletions(ICommandSender sender, String[] arguments) {
        return new String[]{"empty"};
    }

    @Override
    public String description() {
        return "Prints text.";
    }

    @Override
    public String getNamespaceString() {
        return "joecloud";
    }
}
