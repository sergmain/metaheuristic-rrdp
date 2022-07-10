package ai.metaheuristic.rrdp_srv_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Sergio Lissner
 * Date: 7/4/2022
 * Time: 1:56 AM
 */
@Service
@RequiredArgsConstructor
public class CommandService {

    private final DataVerificationService dataVerificationService;

    public boolean startRescanning() {
        dataVerificationService.addVerificationTask();
        dataVerificationService.processVerificationTask();
        return true;
    }
}
