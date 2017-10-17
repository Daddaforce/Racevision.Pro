Feature: Sending a button press
  Scenario: Receiving correct button
    Given the user has pressed a button
    And this has been sent to the receiver
    When the receiver decodes this packet
    Then the receiver will know what button was pressed
    And the receiver will know which boat that maps too

  Scenario: Receiving an incorrect button press
    Given the user has pressed an incorrect button
    And this has been sent to the receiver
    When the receiver decodes this packet
    Then the receiver will know that it is an incorrect button