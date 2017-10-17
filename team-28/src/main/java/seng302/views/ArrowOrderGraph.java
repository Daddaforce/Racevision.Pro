package seng302.views;

import java.util.*;

/**
 * Directed Graph to determine the order of course arrows around the course to be used in the animation.
 * An edge from u to v means u comes right before v in the order.
 */
public class ArrowOrderGraph {

    private Map<Arrow, ArrayList<Arrow>> arrowAdjacencyList;
    private Set<Arrow> currentArrows;
    private Arrow startingArrow;

    ArrowOrderGraph(){
        arrowAdjacencyList = new HashMap<>();
        currentArrows = new HashSet<>();
    }

    /**
     * From the current set of arrows move to the next arrows in order defined by the graph.
     * If the current set is empty, restart at the starting arrow.
     */
    public void moveToNextArrows(){
        if(currentArrows.size() == 0){
            currentArrows.add(startingArrow);
        } else{
            Set<Arrow> nextArrows = new HashSet<>();
            for(Arrow arrow : currentArrows){
                for(Arrow nextArrow : arrowAdjacencyList.get(arrow)){
                    nextArrows.add(nextArrow);
                }
            }
            currentArrows = nextArrows;
        }
    }

    /**
     * Adds an edge in the arrow order graph.
     * @param from The starting arrow
     * @param to The ending arrow
     */
    public void addArrowEdge(Arrow from, Arrow to){
        if(!arrowAdjacencyList.containsKey(from)){
            arrowAdjacencyList.put(from, new ArrayList<>());
        }
        if(!arrowAdjacencyList.containsKey(to)){
            arrowAdjacencyList.put(to, new ArrayList<>());
        }
        arrowAdjacencyList.get(from).add(to);
    }

    public void setStartingArrow(Arrow startingArrow) {
        this.startingArrow = startingArrow;
    }

    public Set<Arrow> getCurrentArrows() {
        return Collections.unmodifiableSet(currentArrows);
    }

    /**
     * Given an arrow list, adds an edge between adjacent arrows in the list
     * @param arrowList A list of arrows in order.
     */
    public void addEdges(List<Arrow> arrowList) {
        for(int i = 1; i < arrowList.size(); i++){
            addArrowEdge(arrowList.get(i-1), arrowList.get(i));
        }
    }

    public Set<Arrow> getAllArrows(){
        return arrowAdjacencyList.keySet();
    }
}
