package ru.nubby.playstream.net;

import java.util.List;

import ru.nubby.playstream.model.Stream;

public interface ResponceListener {
    public void callback (List<Stream> list);
}
