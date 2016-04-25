package com.mammatustech.todo;

import io.advantageous.qbit.annotation.*;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.reakt.reactor.Reactor;

import java.time.Duration;
import java.util.*;

import static io.advantageous.qbit.annotation.QueueCallbackType.*;


@RequestMapping("/todo-service")
public class TodoService {


    private final Map<String, Todo> todoMap = new TreeMap<>();

    /** Used to manage callbacks and such. */
    private final Reactor reactor;

    /** Stats Collector for KPIs. */
    private final StatsCollector statsCollector;

    public TodoService(Reactor reactor, StatsCollector statsCollector) {
        this.reactor = reactor;
        this.statsCollector = statsCollector;

        /** Send stat count i.am.alive every three seconds.  */
        this.reactor.addRepeatingTask(Duration.ofSeconds(3),
                () -> statsCollector.increment("todoservice.i.am.alive"));

        this.reactor.addRepeatingTask(Duration.ofSeconds(1), statsCollector::clientProxyFlush);
    }


    @RequestMapping(value = "/todo", method = RequestMethod.POST)
    public void add(final Callback<Boolean> callback, final Todo todo) {

        /** Send KPI add.called every time the add method gets called. */
        statsCollector.increment("todoservice.add.called");
        todoMap.put(todo.getId(), todo);
        callback.accept(true);
    }



    @RequestMapping(value = "/todo", method = RequestMethod.DELETE)
    public void remove(final Callback<Boolean> callback, final @RequestParam("id") String id) {

        /** Send KPI add.removed every time the remove method gets called. */
        statsCollector.increment("todoservice.remove.called");
        Todo remove = todoMap.remove(id);
        callback.accept(remove!=null);

    }



    @RequestMapping(value = "/todo", method = RequestMethod.GET)
    public void list(final Callback<ArrayList<Todo>> callback) {

        /** Send KPI add.list every time the list method gets called. */
        statsCollector.increment("todoservice.list.called");

        callback.accept(new ArrayList<>(todoMap.values()));
    }


    @QueueCallback({EMPTY, IDLE, LIMIT})
    public void process() {
        reactor.process();
    }


}