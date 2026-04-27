package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Pattern;

public class JsonToNBT
{
    private static final Pattern DOUBLE_PATTERN_NOSUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
    private final String string;
    private int cursor;

    public static NBTTagCompound getTagFromJson(String jsonString) throws NBTException
    {
        return (new JsonToNBT(jsonString)).readSingleStruct();
    }

    @VisibleForTesting
    NBTTagCompound readSingleStruct() throws NBTException
    {
        NBTTagCompound nbttagcompound = readStruct();
        skipWhitespace();

        if (canRead())
        {
            ++cursor;
            throw exception("Trailing data found");
        }
        else
        {
            return nbttagcompound;
        }
    }

    @VisibleForTesting
    JsonToNBT(String stringIn)
    {
        string = stringIn;
    }

    protected String readKey() throws NBTException
    {
        skipWhitespace();

        if (!canRead())
        {
            throw exception("Expected key");
        }
        else
        {
            return peek() == '"' ? readQuotedString() : readString();
        }
    }

    private NBTException exception(String message)
    {
        return new NBTException(message, string, cursor);
    }

    protected NBTBase readTypedValue() throws NBTException
    {
        skipWhitespace();

        if (peek() == '"')
        {
            return new NBTTagString(readQuotedString());
        }
        else
        {
            String s = readString();

            if (s.isEmpty())
            {
                throw exception("Expected value");
            }
            else
            {
                return type(s);
            }
        }
    }

    private NBTBase type(String stringIn)
    {
        try
        {
            if (FLOAT_PATTERN.matcher(stringIn).matches())
            {
                return new NBTTagFloat(Float.parseFloat(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (BYTE_PATTERN.matcher(stringIn).matches())
            {
                return new NBTTagByte(Byte.parseByte(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (LONG_PATTERN.matcher(stringIn).matches())
            {
                return new NBTTagLong(Long.parseLong(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (SHORT_PATTERN.matcher(stringIn).matches())
            {
                return new NBTTagShort(Short.parseShort(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (INT_PATTERN.matcher(stringIn).matches())
            {
                return new NBTTagInt(Integer.parseInt(stringIn));
            }

            if (DOUBLE_PATTERN.matcher(stringIn).matches())
            {
                return new NBTTagDouble(Double.parseDouble(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (DOUBLE_PATTERN_NOSUFFIX.matcher(stringIn).matches())
            {
                return new NBTTagDouble(Double.parseDouble(stringIn));
            }

            if ("true".equalsIgnoreCase(stringIn))
            {
                return new NBTTagByte((byte)1);
            }

            if ("false".equalsIgnoreCase(stringIn))
            {
                return new NBTTagByte((byte)0);
            }
        }
        catch (NumberFormatException var3)
        {
            ;
        }

        return new NBTTagString(stringIn);
    }

    private String readQuotedString() throws NBTException
    {
        int i = ++cursor;
        StringBuilder stringbuilder = null;
        boolean flag = false;

        while (canRead())
        {
            char c0 = pop();

            if (flag)
            {
                if (c0 != '\\' && c0 != '"')
                {
                    throw exception("Invalid escape of '" + c0 + "'");
                }

                flag = false;
            }
            else
            {
                if (c0 == '\\')
                {
                    flag = true;

                    if (stringbuilder == null)
                    {
                        stringbuilder = new StringBuilder(string.substring(i, cursor - 1));
                    }

                    continue;
                }

                if (c0 == '"')
                {
                    return stringbuilder == null ? string.substring(i, cursor - 1) : stringbuilder.toString();
                }
            }

            if (stringbuilder != null)
            {
                stringbuilder.append(c0);
            }
        }

        throw exception("Missing termination quote");
    }

    private String readString()
    {
        int i;

        for (i = cursor; canRead() && isAllowedInKey(peek()); ++cursor)
        {
            ;
        }

        return string.substring(i, cursor);
    }

    protected NBTBase readValue() throws NBTException
    {
        skipWhitespace();

        if (!canRead())
        {
            throw exception("Expected value");
        }
        else
        {
            char c0 = peek();

            if (c0 == '{')
            {
                return readStruct();
            }
            else
            {
                return c0 == '[' ? readList() : readTypedValue();
            }
        }
    }

    protected NBTBase readList() throws NBTException
    {
        return canRead(2) && peek(1) != '"' && peek(2) == ';' ? readArrayTag() : readListTag();
    }

    protected NBTTagCompound readStruct() throws NBTException
    {
        expect('{');
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        skipWhitespace();

        while (canRead() && peek() != '}')
        {
            String s = readKey();

            if (s.isEmpty())
            {
                throw exception("Expected non-empty key");
            }

            expect(':');
            nbttagcompound.setTag(s, readValue());

            if (!hasElementSeparator())
            {
                break;
            }

            if (!canRead())
            {
                throw exception("Expected key");
            }
        }

        expect('}');
        return nbttagcompound;
    }

    private NBTBase readListTag() throws NBTException
    {
        expect('[');
        skipWhitespace();

        if (!canRead())
        {
            throw exception("Expected value");
        }
        else
        {
            NBTTagList nbttaglist = new NBTTagList();
            int i = -1;

            while (peek() != ']')
            {
                NBTBase nbtbase = readValue();
                int j = nbtbase.getId();

                if (i < 0)
                {
                    i = j;
                }
                else if (j != i)
                {
                    throw exception("Unable to insert " + NBTBase.getTagTypeName(j) + " into ListTag of type " + NBTBase.getTagTypeName(i));
                }

                nbttaglist.appendTag(nbtbase);

                if (!hasElementSeparator())
                {
                    break;
                }

                if (!canRead())
                {
                    throw exception("Expected value");
                }
            }

            expect(']');
            return nbttaglist;
        }
    }

    private NBTBase readArrayTag() throws NBTException
    {
        expect('[');
        char c0 = pop();
        pop();
        skipWhitespace();

        if (!canRead())
        {
            throw exception("Expected value");
        }
        else if (c0 == 'B')
        {
            return new NBTTagByteArray(readArray((byte)7, (byte)1));
        }
        else if (c0 == 'L')
        {
            return new NBTTagLongArray(readArray((byte)12, (byte)4));
        }
        else if (c0 == 'I')
        {
            return new NBTTagIntArray(readArray((byte)11, (byte)3));
        }
        else
        {
            throw exception("Invalid array type '" + c0 + "' found");
        }
    }

    private <T extends Number> List<T> readArray(byte p_193603_1_, byte p_193603_2_) throws NBTException
    {
        List<T> list = Lists.<T>newArrayList();

        while (true)
        {
            if (peek() != ']')
            {
                NBTBase nbtbase = readValue();
                int i = nbtbase.getId();

                if (i != p_193603_2_)
                {
                    throw exception("Unable to insert " + NBTBase.getTagTypeName(i) + " into " + NBTBase.getTagTypeName(p_193603_1_));
                }

                if (p_193603_2_ == 1)
                {
                    list.add((T)Byte.valueOf(((NBTPrimitive)nbtbase).getByte()));
                }
                else if (p_193603_2_ == 4)
                {
                    list.add((T)Long.valueOf(((NBTPrimitive)nbtbase).getLong()));
                }
                else
                {
                    list.add((T)Integer.valueOf(((NBTPrimitive)nbtbase).getInt()));
                }

                if (hasElementSeparator())
                {
                    if (!canRead())
                    {
                        throw exception("Expected value");
                    }

                    continue;
                }
            }

            expect(']');
            return list;
        }
    }

    private void skipWhitespace()
    {
        while (canRead() && Character.isWhitespace(peek()))
        {
            ++cursor;
        }
    }

    private boolean hasElementSeparator()
    {
        skipWhitespace();

        if (canRead() && peek() == ',')
        {
            ++cursor;
            skipWhitespace();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void expect(char expected) throws NBTException
    {
        skipWhitespace();
        boolean flag = canRead();

        if (flag && peek() == expected)
        {
            ++cursor;
        }
        else
        {
            throw new NBTException("Expected '" + expected + "' but got '" + (flag ? peek() : "<EOF>") + "'", string, cursor + 1);
        }
    }

    protected boolean isAllowedInKey(char charIn)
    {
        return charIn >= '0' && charIn <= '9' || charIn >= 'A' && charIn <= 'Z' || charIn >= 'a' && charIn <= 'z' || charIn == '_' || charIn == '-' || charIn == '.' || charIn == '+';
    }

    private boolean canRead(int p_193608_1_)
    {
        return cursor + p_193608_1_ < string.length();
    }

    boolean canRead()
    {
        return canRead(0);
    }

    private char peek(int p_193597_1_)
    {
        return string.charAt(cursor + p_193597_1_);
    }

    private char peek()
    {
        return peek(0);
    }

    private char pop()
    {
        return string.charAt(cursor++);
    }
}
