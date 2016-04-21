/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.web.model.beans;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.application.algorithm.data.number.TimeDistanceFactory;
import cz.certicon.routing.data.basic.FileSource;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.xml.XmlCoordinateReader;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.xml.XmlGraphReader;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.data.nodesearch.xml.GraphNodeSearcher;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.neighbourlist.DirectedNeighborListGraphEntityFactory;
import cz.certicon.routing.web.model.Priority;
import cz.certicon.routing.web.model.Settings;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
@Component
@Scope( "singleton" )
public class GraphBean implements Serializable {

    private Graph graph;
    private CoordinateReader coordinateReader;
    private NodeSearcher nodeSearcher;
    private Priority priority;
    private GraphEntityFactory graphEntityFactory;
    private DistanceFactory distanceFactory;

    @Autowired
    private DatabasePropertiesBean databasePropertiesBean;

    public GraphBean() {
        priority = Priority.LENGTH;
        this.graphEntityFactory = new DirectedNeighborListGraphEntityFactory();
        this.distanceFactory = new LengthDistanceFactory();
    }

    public void setPriority( Priority priority ) {
        if ( this.priority != priority ) {
            switch ( priority ) {
                case LENGTH:
                    distanceFactory = new LengthDistanceFactory();
                    break;
                case TIME:
                    distanceFactory = new TimeDistanceFactory();
                    break;
            }
            graph = null;
            System.gc();
        }
        this.priority = priority;
    }

    public Graph getGraph() throws IOException {
        if ( graph == null ) {
            GraphReader graphReader = new XmlGraphReader( new FileSource( new File( Settings.GRAPH_FILE_PATH ) ) );//= new DatabaseGraphRW( databasePropertiesBean.getConnectionProperties() );
            graph = graphReader.read( new Pair<>( graphEntityFactory, distanceFactory ) );
            graphReader.close();
        }
        return graph;
    }

    public Map<Edge, List<Coordinates>> getCoordinates( Set<Edge> edges ) throws IOException {
        if ( coordinateReader == null ) {
            coordinateReader = new XmlCoordinateReader( new FileSource( new File( Settings.COORDINATES_FILE_PATH ) ) );//= new DatabaseCoordinatesRW( databasePropertiesBean.getConnectionProperties() );
        }
        coordinateReader.open();
        Map<Edge, List<Coordinates>> edgeMap = coordinateReader.read( edges );
        coordinateReader.close();
        return edgeMap;
    }

    public Map<Coordinates, Distance> getClosestNodes( Coordinates coords ) throws IOException {
        if ( nodeSearcher == null ) {
            nodeSearcher = new GraphNodeSearcher( getGraph() );
        }
        return nodeSearcher.findClosestNodes( coords, distanceFactory );
    }

    public GraphEntityFactory getGraphEntityFactory() {
        return graphEntityFactory;
    }

    public DistanceFactory getDistanceFactory() {
        return distanceFactory;
    }
}
