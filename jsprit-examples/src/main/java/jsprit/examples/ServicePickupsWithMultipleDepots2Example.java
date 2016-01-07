/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.examples;

import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.util.Examples;

import java.util.Collection;


public class ServicePickupsWithMultipleDepots2Example {

    public static void main(String[] args) {
        /*
         * some preparation - create output folder
		 */
        Examples.createOutputFolder();

		/*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType1").addCapacityDimension(0, 8);
        vehicleTypeBuilder.setCostPerDistance(1.0);
        VehicleType vehicleType = vehicleTypeBuilder.build();

        VehicleTypeImpl.Builder vehicleTypeBuilder2 = VehicleTypeImpl.Builder.newInstance("vehicleType2").addCapacityDimension(0, 8);
        vehicleTypeBuilder2.setCostPerDistance(1.0);
        VehicleType vehicleType2 = vehicleTypeBuilder2.build();

		/*
         * define two depots, i.e. two vehicle locations ([10,10],[50,50]) and equip them with an infinite number of vehicles of type 'vehicleType'
		 */
        Builder vehicleBuilder1 = Builder.newInstance("vehicles@[10,10]");
        vehicleBuilder1.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder1.setType(vehicleType);
        VehicleImpl vehicle1 = vehicleBuilder1.build();

        Builder vehicleBuilder2 = Builder.newInstance("vehicles@[50,50]");
        vehicleBuilder2.setStartLocation(Location.newInstance(50, 50));
        vehicleBuilder2.setType(vehicleType2);
        VehicleImpl vehicle2 = vehicleBuilder2.build();


		/*
         * build shipments at the required locations, each with a capacity-demand of 1.
		 * 4 shipments
		 * 1: (5,7)->(6,9)
		 * 2: (5,13)->(6,11)
		 * 3: (15,7)->(14,9)
		 * 4: (15,13)->(14,11)
		 */

        Service shipment1 = Service.Builder.newInstance("1")
            .setServiceTime(20.).addSizeDimension(0, 1).setLocation(Location.newInstance(5, 7)).build();
        Service shipment2 = Service.Builder.newInstance("2")
            .setServiceTime(20.).addSizeDimension(0, 1).setLocation(Location.newInstance(5, 13)).build();

        Service shipment3 = Service.Builder.newInstance("3")
            .setServiceTime(20).addSizeDimension(0, 1).setLocation(Location.newInstance(15, 7)).build();
        Service shipment4 = Service.Builder.newInstance("4")
            .setServiceTime(20.).addSizeDimension(0, 1).setLocation(Location.newInstance(15, 13)).build();

        Service shipment5 = Service.Builder.newInstance("5")
            .setServiceTime(20).addSizeDimension(0, 1).setLocation(Location.newInstance(55, 57)).build();
        Service shipment6 = Service.Builder.newInstance("6")
            .setServiceTime(20).addSizeDimension(0, 1).setLocation(Location.newInstance(55, 63)).build();

        Service shipment7 = Service.Builder.newInstance("7")
            .setServiceTime(20).addSizeDimension(0, 1).setLocation(Location.newInstance(65, 57)).build();
        Service shipment8 = Service.Builder.newInstance("8")
            .setServiceTime(20).addSizeDimension(0, 1).setLocation(Location.newInstance(65, 63)).build();


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle1).addVehicle(vehicle2);
        vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4);
        vrpBuilder.addJob(shipment5).addJob(shipment6).addJob(shipment7).addJob(shipment8);

        vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                if(vehicle.getType().getTypeId().equals("vehicleType1")){
                    return tourAct.getOperationTime();
                }
                else return tourAct.getOperationTime() / 10.;
            }

        });
//		vrpBuilder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem problem = vrpBuilder.build();

		/*
         * get the algorithm out-of-the-box.
		 */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        algorithm.setMaxIterations(100);

		/*
         * and search a solution
		 */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

		/*
         * get the best
		 */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

		/*
		 * print nRoutes and totalCosts of bestSolution
		 */
        SolutionPrinter.print(problem,bestSolution, SolutionPrinter.Print.VERBOSE);

		/*
		 * plot problem with solution
		 */
        Plotter solutionPlotter = new Plotter(problem, bestSolution);
        solutionPlotter.plot("output/plot.png", "vehicle dependent service time");


    }

}
