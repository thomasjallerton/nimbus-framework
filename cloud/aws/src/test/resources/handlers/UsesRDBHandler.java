package handlers;

import com.nimbusframework.nimbuscore.annotations.database.UsesRelationalDatabase;
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.RelationalDatabaseModel;

public class UsesRDBHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesRelationalDatabase(dataModel = RelationalDatabaseModel.class)
    public void func() {
        ClientBuilder.getDatabaseClient(RelationalDatabaseModel.class);
    }

}
