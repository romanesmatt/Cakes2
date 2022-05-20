package cakes.akkaCakes;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.pattern.Patterns;
import cakes.akkaUtils.AkkaConfig;
import cakes.dataCakes.Cake;
import cakes.dataCakes.Gift;
import cakes.dataCakes.Sugar;
import cakes.dataCakes.Wheat;

@SuppressWarnings("serial")
class GiftRequest implements Serializable {
}
//--------

/**
 * Producer class as outlined for Task 2.
 *
 * @param <T>
 */
abstract class Producer<T> extends AbstractActor {
    public abstract CompletableFuture<T> make();
}


class Alice extends Producer<Wheat> {
    Queue<Wheat> products = new ArrayDeque<>();

    int maxSize;
    boolean running = true;

    public Alice(int maxSize) {
        this.maxSize = maxSize;
    }

    public Receive createReceive() {
        return receiveBuilder()

                .match(Wheat.class, T -> products.add(T))
                .match(String.class, s -> s.equalsIgnoreCase("MakeOne"), s -> {
                    if (products.size() >= maxSize) { //List is full
                        running = false;
                    } else { //List is not full
                        ActorRef me = self();
                        CompletableFuture<Wheat> futureProduct = make();
                        futureProduct.thenAcceptAsync(wheat -> {
                            me.tell(wheat, self());
                            self().tell("MakeOne", self());
                        });
                    }
                })
                .match(String.class, s -> s.equalsIgnoreCase("GiveOne"), s -> { //GiveOne
                    ActorRef sender = sender();

                    if (products.isEmpty()) { //If the list is empty, make one and send it in the future
                        make().thenAcceptAsync(wheat -> sender.tell(wheat, self()));
                    } else {
                        if (!running) {
                            running = true;
                            self().tell("MakeOne", self());
                        }
                        sender.tell(products.poll(), self()); //Sending Wheat object
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Wheat> make() {
        return CompletableFuture.supplyAsync(Wheat::new);
    }
}

class Bob extends Producer<Sugar> {
    Queue<Sugar> products = new ArrayDeque<>();

    int maxSize;
    boolean running = true;

    public Bob(int maxSize) {
        this.maxSize = maxSize;
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(Sugar.class, T -> products.add(T))
                .match(String.class, s -> s.equalsIgnoreCase("MakeOne"), s -> {
                    if (products.size() >= maxSize) { //List is full
                        running = false;
                    } else { //List is not full
                        ActorRef me = self();
                        CompletableFuture<Sugar> futureProduct = make();
                        futureProduct.thenAcceptAsync(sugar -> {
                            me.tell(sugar, self());
                            self().tell("MakeOne", self());
                        });
                    }
                })
                .match(String.class, s -> s.equalsIgnoreCase("GiveOne"), s -> {
                    ActorRef sender = sender();
                    if (products.isEmpty()) { //If the list is empty, make one and send it in the future
                        make().thenAcceptAsync(sugar -> sender.tell(sugar, self()));
                    } else {
                        if (!running) {
                            running = true;
                            self().tell("MakeOne", self());
                        }
                        sender.tell(products.poll(), self()); //Sending Sugar object
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Sugar> make() {
        return CompletableFuture.supplyAsync(Sugar::new);
    }
}

class Charles extends Producer<Cake> {
    Queue<Wheat> ws = new ArrayDeque<>();//no synchronization issues!
    Queue<Sugar> ss = new ArrayDeque<>();//no synchronization issues!

    Queue<Cake> products = new ArrayDeque<>();

    int maxSize;

    ActorRef alice;
    //    Bob object for Task 2
    ActorRef bob;

    //    The four Bob objects
    ActorRef bobOne;
    ActorRef bobTwo;
    ActorRef bobThree;
    ActorRef bobFour;

    boolean isRunning = true;

    //    Allows the option of running Task 3 with the four Bob objects
    boolean areFourBobs = false;


    //    Constructor for Task 2
//    Comment out for Task 3

//    public Charles(int maxSize, ActorRef alice, ActorRef bob) {
//        this.maxSize = maxSize;
//        this.alice = alice;
//        this.bob = bob;
//    }

    //    Constructor for Task 3
//    Comment out for Task 2

    public Charles(int maxSize, ActorRef alice, ActorRef bobOne, ActorRef bobTwo, ActorRef bobThree, ActorRef bobFour) {
        this.maxSize = maxSize;
        this.alice = alice;
        this.bobOne = bobOne;
        this.bobTwo = bobTwo;
        this.bobThree = bobThree;
        this.bobFour = bobFour;
        this.areFourBobs = true;
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(Wheat.class, T -> ws.add(T))
                .match(Sugar.class, T -> ss.add(T))
                .match(Cake.class, T -> products.add(T))
                .match(String.class, s -> s.equalsIgnoreCase("MakeOne"), s -> {
                    //First, get the ingredients
                    CompletableFuture<?> getWheat = Patterns.ask(alice, "GiveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

//                    getSugar() method for Bob object in Task 2
                    //Comment out for Task 3
                    CompletableFuture<?> getSugarTaskTwo = Patterns.ask(bob, "GiveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

//                    getSugar() methods for the Bob objects in Task 3
                    //Comment out for Task 2

                    CompletableFuture<?> getSugarOne = Patterns.ask(bobOne, "GiveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    CompletableFuture<?> getSugarTwo = Patterns.ask(bobTwo, "GiveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    CompletableFuture<?> getSugarThree = Patterns.ask(bobThree, "GiveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    CompletableFuture<?> getSugarFour = Patterns.ask(bobFour, "GiveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    //Second, make the cakes
                    if (products.size() >= maxSize) { //List is full
                        isRunning = false;
                    } else { //List is not full
                        ActorRef me = self();
                        CompletableFuture<Cake> futureProduct = make();
                        futureProduct.thenAcceptAsync(cake -> {
                            me.tell(cake, self());
                            self().tell("MakeOne", self());
                        });
                    }
                })
                .match(String.class, s -> s.equalsIgnoreCase("GiveOne"), s -> {
                    ActorRef sender = sender();
                    if (products.isEmpty()) { //If the list is empty, make one and send it in the future
                        make().thenAcceptAsync(cake -> sender.tell(cake, self()));
                    } else {
                        if (!isRunning) {
                            isRunning = true;
                            self().tell("MakeOne", self());
                        }
                        sender.tell(products.poll(), self()); //Sending Cake object
                    }
                }).build();
    }

    @Override
    public CompletableFuture<Cake> make() {
        return CompletableFuture.supplyAsync(() -> new Cake(ss.poll(), ws.poll()));
    }
}

class Tim extends AbstractActor {
    int hunger;
    boolean running = true;
    ActorRef cakeMan;
    ActorRef originalSender;

    public Tim(int hunger, ActorRef cakeMan) {
        this.hunger = hunger;
        this.cakeMan = cakeMan;
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(GiftRequest.class, () -> running, gr -> {
                    //Main thread who wants return
                    originalSender = sender();

                    CompletableFuture<?> getCake = Patterns.ask(cakeMan, "GiveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    CompletableFuture<String> eatCake =
                            CompletableFuture.allOf(getCake)
                                    .thenApplyAsync(v -> {
                                        self().tell((Cake) getCake.join(), cakeMan);

                                        return "Thanks! But I'm still hungry..." + hunger;
                                    });
                })
                .match(Cake.class, () -> running, c -> {
                    hunger -= 1;

                    if (hunger > 0) {
                        self().tell(new GiftRequest(), originalSender);
                        return;
                    }
                    running = false;
                    originalSender.tell(new Gift(), self());
                }).build();
    }
}

public class Cakes {
    public static void main(String[] args) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Gift g = computeGift(1000);
        assert g != null;
        System.out.println(
                "\n\n-----------------------------\n\n" +
                        g +
                        "\n\n-----------------------------\n\n");
    }

    public static Gift computeGift(int hunger) {
        ActorSystem s = AkkaConfig.newSystem("Cakes", 2501, Map.of(
//                Comment out IP Addresses to work on assignment locally.
                "Tim", "192.168.56.1",
                "Bob", "192.168.56.1",
                "Charles", "192.168.56.1"
                //Alice stays local
        ));

        ActorRef alice = s.actorOf(Props.create(Alice.class, () -> new Alice(1000)), "Alice"); //makes wheat

//        Comment out for Task 3
        ActorRef bob = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "Bob"); //makes sugar


//        Create four Bob objects to test on local machine
        //        Comment out for Task 2
        ActorRef bobOne = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobOne"); //makes sugar
        ActorRef bobTwo = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobTwo"); //makes sugar
        ActorRef bobThree = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobThree"); //makes sugar
        ActorRef bobFour = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobFour"); //makes sugar


        //Makes cake with wheat and sugar
//        Comment out for Task 3
        ActorRef charles = s.actorOf(Props.create(Charles.class, () -> new Charles(1000, bobOne, bobTwo, bobThree, bobFour, alice)), "Charles");

//        ActorRef charles = s.actorOf(Props.create(Charles.class, () -> new Charles(1000, bob, alice)), "Charles");

        //tim wants to eat cakes
        ActorRef tim = s.actorOf(Props.create(Tim.class, () -> new Tim(hunger, charles)), "Tim");

        //Begin measuring the time taken to complete
        long initialTimeStamp = System.currentTimeMillis();

        CompletableFuture<Object> gift = Patterns.ask(tim, new GiftRequest(), Duration.ofMillis(100000000)).toCompletableFuture();

        try {
            //Waiting until the gift task is complete
            return (Gift) gift.join();
        } finally {
            System.out.println("Result: " + (System.currentTimeMillis() - initialTimeStamp));
            //When complete, murder them all with a PoisonPill sent by no one.
            alice.tell(PoisonPill.getInstance(), ActorRef.noSender());
//            Task 2: Bob object
            bob.tell(PoisonPill.getInstance(), ActorRef.noSender());
//            Task 3: Four Bob objects only for running on local machine
            bobOne.tell(PoisonPill.getInstance(), ActorRef.noSender());
            bobTwo.tell(PoisonPill.getInstance(), ActorRef.noSender());
            bobThree.tell(PoisonPill.getInstance(), ActorRef.noSender());
            bobFour.tell(PoisonPill.getInstance(), ActorRef.noSender());
            charles.tell(PoisonPill.getInstance(), ActorRef.noSender());
            tim.tell(PoisonPill.getInstance(), ActorRef.noSender());
            s.terminate();
        }
    }
}