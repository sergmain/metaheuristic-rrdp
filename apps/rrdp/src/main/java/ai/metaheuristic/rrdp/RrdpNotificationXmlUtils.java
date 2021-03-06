package ai.metaheuristic.rrdp;

import lombok.SneakyThrows;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Consumer;

import static ai.metaheuristic.rrdp.RrdpCommonUtils.isAny;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 4:45 AM
 */
@SuppressWarnings("WeakerAccess")
public class RrdpNotificationXmlUtils {

    private final static XMLInputFactory XML_FACTORY = XMLInputFactory.newInstance();

    public static RrdpNotificationXml parseNotificationXml(String xml) {
        StringReader reader = new StringReader(xml);
        return parseNotificationXml(reader);
    }

    @SneakyThrows
    public static RrdpNotificationXml parseNotificationXml(Reader reader) {
        XMLEventReader eventReader = XML_FACTORY.createXMLEventReader(reader);
        RrdpNotificationXml notification = parse(eventReader);
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

    private static RrdpNotificationXml parse(XMLEventReader eventReader) {
        RrdpNotificationXml notification = new RrdpNotificationXml();

        EventIterator<XMLEventReader, Consumer<XMLEvent>> eventIterator = RrdpNotificationXmlUtils::iterateEvents;

        eventIterator.accept(eventReader, event -> {
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    final String elementName = startElement.getName().getLocalPart();
                    if ("notification".equals(elementName)) {
                        initAttrForNotification(startElement.getAttributes(), notification);
                    }
                    else if (isAny(elementName, "snapshot", "delta")) {
                        RrdpNotificationXml.Entry entry = new RrdpNotificationXml.Entry();
                        entry.type = "snapshot".equals(elementName) ? RrdpEnums.NotificationEntryType.SNAPSHOT : RrdpEnums.NotificationEntryType.DELTA;
                        notification.entries.add(entry);
                        initAttrForEntry(startElement.getAttributes(), entry);
                    }
                    else {
                        throw new IllegalStateException("Malformed xml, unknown xml element: " + elementName );
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.END_ELEMENT:
                    break;
            }
        });
        return notification;
    }

    private static void initAttrForEntry(Iterator<Attribute> attributes, RrdpNotificationXml.Entry entry) {
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            final String attrName = attribute.getName().getLocalPart();
            switch (attrName) {
                case "serial":
                    entry.serial = Integer.parseInt(attribute.getValue());
                    break;
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

    private static void initAttrForNotification(Iterator<Attribute> attributes, RrdpNotificationXml notification) {
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            final String attrName = attribute.getName().getLocalPart();
            switch (attrName) {
                case "session_id":
                    notification.sessionId = attribute.getValue();
                    break;
                case "serial":
                    notification.serial = Integer.parseInt(attribute.getValue());
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
