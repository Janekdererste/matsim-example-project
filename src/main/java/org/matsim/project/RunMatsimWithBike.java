package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.Set;

public class RunMatsimWithBike {

    public static void main(String[] args) {

        Config config;
        if (args == null || args.length == 0 || args[0] == null) {
            config = ConfigUtils.loadConfig("scenarios/equil/config.xml");
        } else {
            System.out.println("laoding config from: " + args[0]);
            config = ConfigUtils.loadConfig(args);
        }
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);

        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);
        config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.kinematicWaves);

        // possibly modify config here

        // ---

        Scenario scenario = ScenarioUtils.loadScenario(config);

        // possibly modify scenario here

        // ---

        var innerLinks = Set.of(
                Id.createLinkId(3),
                Id.createLinkId(4),
                Id.createLinkId(5),
                Id.createLinkId(6),
                Id.createLinkId(7),
                Id.createLinkId(8),
                Id.createLinkId(9)
        );

        for (Link link : scenario.getNetwork().getLinks().values()) {

            // the original network has different lengths set eucledean length for our example
            link.setLength(CoordUtils.calcEuclideanDistance(link.getFromNode().getCoord(), link.getToNode().getCoord()));

            if (innerLinks.contains(link.getId())) {
                link.setAllowedModes(Set.of(TransportMode.bike));
            } else {
                link.setAllowedModes(Set.of(TransportMode.car, TransportMode.bike));
            }
        }

        // remove routes from legs, since the network has changed
        scenario.getPopulation().getPersons().values().stream()
                .map(HasPlansAndId::getSelectedPlan)
                .flatMap(plan -> TripStructureUtils.getLegs(plan).stream())
                .forEach(leg -> leg.setRoute(null));


        Controler controler = new Controler(scenario);

        // possibly modify controler here

        //controler.addOverridingModule( new OTFVisLiveModule() ) ;


        // ---

        controler.run();
    }
}
