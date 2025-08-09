package ai.metaheuristic.rrdp.srv_service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/22/2022
 * Time: 9:39 PM
 */
public class ContentServiceTest {

    @Test
    public void test_55() {
        String s = "/edition/statistics-unpacked/2020-07/2020-07-14/RLAW355/59563/59563_RLAW355%20-%20%C3%90%C2%BC%C3%90%C2%BE%C3%91%C2%8F.xml";


        String decodedPath = ContentService.decodePath(s);
        System.out.println(decodedPath);


        assertEquals("/edition/statistics-unpacked/2020-07/2020-07-14/RLAW355/59563/59563_RLAW355 - Ð¼Ð¾Ñ\u008F.xml", decodedPath);
    }
}
