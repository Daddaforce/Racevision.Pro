Feature: Sending and understanding key presses

  Scenario: User presses enter key to tack their boat
    Given Sally has a boat
    When Sally presses the "Enter" key
    Then the heading of Sally's boat has been changed

  Scenario: User presses shift key to luff
    Given Sally has a boat
    When Sally presses the "Shift" key
    Then the sails should be brought in so that the speed becomes 0

  Scenario: User presses the space key to go on the VMG line
    Given Sally has a boat
    When Sally presses the "Space" key
    Then the boats heading should move towards the optimal angle

  Scenario: User presses the page up key to rotate the boat
    Given Sally has a boat
    When Sally presses the "Page_up" key
    Then the boats heading should be decreased

  Scenario: User presses the page down key to rotate the boat
    Given Sally has a boat
    When Sally presses the "Page_down" key
    Then the boats heading should be increased


