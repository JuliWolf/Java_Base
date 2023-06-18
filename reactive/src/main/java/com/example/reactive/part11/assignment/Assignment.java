package com.example.reactive.part11.assignment;

import com.example.reactive.utils.Util;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Assignment {
  public static void main(String[] args) {
    SlackRoom slackRoom = new SlackRoom("reactor");

    SlackMember sam = new SlackMember("Sam");
    SlackMember jake = new SlackMember("Jake");
    SlackMember mike = new SlackMember("Mike");

    slackRoom.joinRoom(sam);
    slackRoom.joinRoom(jake);

    sam.says("Hi all..");
    Util.sleepSeconds(4);

    jake.says("Hey!");
    sam.says("I simply wanted to say hi..");
    Util.sleepSeconds(4);

    slackRoom.joinRoom(mike);
    mike.says("Hey guys.. glad to be here");
    Util.sleepSeconds(4);
  }
}
