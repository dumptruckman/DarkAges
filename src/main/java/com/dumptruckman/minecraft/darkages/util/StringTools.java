package com.dumptruckman.minecraft.darkages.util;

import java.util.ArrayList;
import java.util.List;

public class StringTools {

    public static String[] joinArgs(final String args) {
        return joinArgs(args.split(" "));
    }

    public static String[] joinArgs(final String[] args) {
        // Eliminate empty args and combine multiword args first
        List<String> argList = new ArrayList<String>(args.length);
        for (int i = 1; i < args.length; ++i) {
            String arg = args[i];
            if (arg.length() == 0) {
                continue;
            }

            switch (arg.charAt(0)) {
            case '\'':
            case '"':
                final StringBuilder build = new StringBuilder();
                final char quotedChar = arg.charAt(0);

                int endIndex;
                for (endIndex = i; endIndex < args.length; ++endIndex) {
                    final String arg2 = args[endIndex];
                    if (arg2.charAt(arg2.length() - 1) == quotedChar && arg2.length() > 1) {
                        if (endIndex != i) build.append(' ');
                        build.append(arg2.substring(endIndex == i ? 1 : 0, arg2.length() - 1));
                        break;
                    } else if (endIndex == i) {
                        build.append(arg2.substring(1));
                    } else {
                        build.append(' ').append(arg2);
                    }
                }

                if (endIndex < args.length) {
                    arg = build.toString();
                    i = endIndex;
                }

                // In case there is an empty quoted string
                if (arg.length() == 0) {
                    continue;
                }
                // else raise exception about hanging quotes?
            }
            argList.add(arg);
        }
        return argList.toArray(new String[argList.size()]);
    }
}
