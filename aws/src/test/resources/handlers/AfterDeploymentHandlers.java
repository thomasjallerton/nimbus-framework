package handlers;

import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment;

public class AfterDeploymentHandlers {

  @AfterDeployment
  public String addSubscription() {
    return "This could do something";
  }

}
