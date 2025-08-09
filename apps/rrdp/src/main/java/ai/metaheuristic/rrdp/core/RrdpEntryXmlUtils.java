package ai.metaheuristic.rrdp.core;

import lombok.SneakyThrows;

import javax.annotation.Nullable;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.function.Consumer;

import static ai.metaheuristic.rrdp.core.RrdpCommonUtils.isAny;

/**
 * @author Sergio Lissner
 * Date: 6/28/2022
 * Time: 2:58 PM
 */
public class RrdpEntryXmlUtils {

    private final static XMLInputFactory XML_FACTORY = XMLInputFactory.newInstance();

    @SneakyThrows
    public static RrdpEntryXml parseRrdpEntryXml(InputStream inputStream) {
        XMLEventReader eventReader = XML_FACTORY.createXMLEventReader(inputStream, StandardCharsets.UTF_8.toString());
        RrdpEntryXml notification = parse(eventReader);
        return notification;
    }

    @FunctionalInterface
    interface EventIterator<E, C> {
        void accept(E e, C c);
    }

    @SneakyThrows
    private static void iterateEvents(XMLEventReader eventReader, Consumer<XMLEvent> consumer) {
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            consumer.accept(event);
        }
    }

    private static class RrdpEntryXmlEntryHolder {
        @Nullable
        public RrdpEntryXml.Entry entry;
    }

    private static RrdpEntryXml parse(XMLEventReader eventReader) {
        RrdpEntryXml entryXml = new RrdpEntryXml();

        EventIterator<XMLEventReader, Consumer<XMLEvent>> eventIterator = RrdpEntryXmlUtils::iterateEvents;

        final RrdpEntryXmlEntryHolder current = new RrdpEntryXmlEntryHolder();

        eventIterator.accept(eventReader, event -> {
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement;
                    startElement = event.asStartElement();
                    final String elementName = startElement.getName().getLocalPart();
                    if (isAny(elementName, "delta", "snapshot")) {
                        initAttrForEntryXml(startElement.getAttributes(), entryXml);
                    }
                    else if (isAny(elementName, "publish", "withdraw", "update")) {
                        current.entry = new RrdpEntryXml.Entry();
                        current.entry.state = RrdpEnums.EntryState.to(elementName);
                        entryXml.entries.add(current.entry);
                        initAttrForEntry(startElement.getAttributes(), current.entry);
                    }
                    else {
                        throw new IllegalStateException("Malformed xml, unknown xml element: " + elementName );
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    if (current.entry!=null) {
                        String characters = event.asCharacters().getData();
                        current.entry.add(characters);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    current.entry = null;
                    break;
            }
        });
        return entryXml;
    }

    private static void initAttrForEntry(Iterator<Attribute> attributes, RrdpEntryXml.Entry entry) {
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            final String attrName = attribute.getName().getLocalPart();
            switch (attrName) {
                case "uri":
                    entry.uri = attribute.getValue();
                    break;
                case "hash":
                    entry.hash = attribute.getValue();
                    break;
                case "length":
                    entry.length = Integer.parseInt(attribute.getValue());
                    break;
                default:
                    throw new IllegalStateException("Malformed xml, unknown xml attribute: " + attrName);
            }
        }
    }

    private static void initAttrForEntryXml(Iterator<Attribute> attributes, RrdpEntryXml entryXml) {
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            final String attrName = attribute.getName().getLocalPart();
            switch (attrName) {
                case "session_id":
                    entryXml.sessionId = attribute.getValue();
                    break;
                case "serial":
                    entryXml.serial = Integer.parseInt(attribute.getValue());
                    break;
                case "version":
                case "xmlns":
                    break;
                default:
                    throw new IllegalStateException("Malformed xml, unknown xml attribute: " + attrName);
            }
        }
    }

}
