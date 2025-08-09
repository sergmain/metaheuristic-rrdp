package ai.metaheuristic.rrdp.srv_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 7/4/2022
 * Time: 1:56 AM
 */
@Service
@RequiredArgsConstructor
public class CommandService {

    private final DataVerificationService dataVerificationService;
    private final ContentService contentService;

    public boolean startRescanning(String code, List<String> paths) {
        dataVerificationService.addVerificationTask(new RrdpData.TaskParams(code, paths));
        dataVerificationService.processVerificationTask();
        return true;
    }

    public void startAllRescanning() {
        for (String code : contentService.getDataCodes()) {
            dataVerificationService.addVerificationTask(new RrdpData.TaskParams(code, List.of()));
        }
        dataVerificationService.processVerificationTask();
    }

    public static RrdpData.RrdpServerStatus status() {
        return DataVerificationService.status();
    }
}
