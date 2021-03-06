/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.vehiclerouting.plugin.planner;

import org.junit.jupiter.api.Test;
import org.optaweb.vehiclerouting.Profiles;
import org.optaweb.vehiclerouting.plugin.planner.domain.DistanceMap;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningDepot;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningLocation;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningLocationFactory;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningVehicle;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningVehicleFactory;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningVisitFactory;
import org.optaweb.vehiclerouting.plugin.planner.domain.VehicleRoutingSolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.optaweb.vehiclerouting.plugin.planner.Constants.SOLVER_CONFIG;
import static org.optaweb.vehiclerouting.plugin.planner.domain.SolutionFactory.solutionFromVisits;

@SpringBootTest(
        properties = {
                "optaplanner.solver-config-xml=" + SOLVER_CONFIG,
                "optaplanner.solver.termination.spent-limit=100ms"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(Profiles.TEST)
class SolverManagerIntegrationTest {

    @Autowired
    private SolverManager solverManager;

    private static DistanceMap mockDistanceMap() {
        return location -> 60;
    }

    @Test
    void solver_should_be_in_daemon_mode() throws InterruptedException {
        PlanningVehicle vehicle = PlanningVehicleFactory.testVehicle(1);
        PlanningLocation depot = PlanningLocationFactory.testLocation(1, mockDistanceMap());
        PlanningLocation visit = PlanningLocationFactory.testLocation(2, mockDistanceMap());
        VehicleRoutingSolution solution = solutionFromVisits(
                singletonList(vehicle),
                new PlanningDepot(depot),
                singletonList(PlanningVisitFactory.fromLocation(visit))
        );
        solverManager.startSolver(solution);

        Thread.sleep(1000);

        // This will check that solver is still running. If daemon was set to false, solver would have terminated
        // due to 100ms time spent termination and the isAlive check would fail.
        assertThatCode(() -> solverManager.changeCapacity(vehicle)).doesNotThrowAnyException();
    }
}
