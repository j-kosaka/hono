/**
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial creation
 */

package org.eclipse.hono.service.registration;

import static java.net.HttpURLConnection.*;

import org.eclipse.hono.util.Constants;
import org.eclipse.hono.util.RegistrationConstants;
import org.eclipse.hono.util.RegistrationResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;


/**
 * BaseRegistrationServiceTest
 *
 */
@RunWith(VertxUnitRunner.class)
public class BaseRegistrationServiceTest {

    private static String secret = "secret";

    /**
     * Verifies that an enabled device's status can be asserted successfully.
     * 
     * @param ctx The vertx unit test context.
     */
    @Test(timeout = 1000)
    public void testAssertDeviceRegistrationReturnsToken(final TestContext ctx) {

        // GIVEN a registry that contains an enabled device
        BaseRegistrationService registrationService = getRegistrationService(HTTP_OK, BaseRegistrationService.getResultPayload("4711", new JsonObject()));
        registrationService.setSigningSecret(secret);

        // WHEN trying to assert the device's registration status
        registrationService.assertRegistration(Constants.DEFAULT_TENANT, "4711", ctx.asyncAssertSuccess(result -> {
            // THEN the response contains a JWT token asserting the device's registration status
            ctx.assertEquals(result.getStatus(), HTTP_OK);
            JsonObject payload = result.getPayload();
            ctx.assertNotNull(payload);
            String compactJws = payload.getString(RegistrationConstants.FIELD_ASSERTION);
            ctx.assertNotNull(compactJws);
        }));
    }

    /**
     * Verifies that a disabled device's status cannot be asserted.
     * 
     * @param ctx The vertx unit test context.
     */
    @Test(timeout = 1000)
    public void testAssertDeviceRegistrationFailsForDisabledDevice(final TestContext ctx) {

        // GIVEN a registry that contains an enabled device
        RegistrationService registrationService = getRegistrationService(
                HTTP_OK, BaseRegistrationService.getResultPayload("4711", new JsonObject().put(RegistrationConstants.FIELD_ENABLED, false)));

        // WHEN trying to assert the device's registration status
        registrationService.assertRegistration(Constants.DEFAULT_TENANT, "4711", ctx.asyncAssertSuccess(result -> {
            // THEN the response does not contain a JWT token
            ctx.assertEquals(result.getStatus(), HTTP_NOT_FOUND);
            ctx.assertNull(result.getPayload());
        }));
    }

    /**
     * Verifies that a non existing device's status cannot be asserted.
     * 
     * @param ctx The vertx unit test context.
     */
    @Test(timeout = 1000)
    public void testAssertDeviceRegistrationFailsForNonExistingDevice(final TestContext ctx) {

        // GIVEN a registry that contains an enabled device
        RegistrationService registrationService = getRegistrationService(HTTP_NOT_FOUND, null);

        // WHEN trying to assert the device's registration status
        registrationService.assertRegistration(Constants.DEFAULT_TENANT, "4711", ctx.asyncAssertSuccess(result -> {
            // THEN the response does not contain a JWT token
            ctx.assertEquals(result.getStatus(), HTTP_NOT_FOUND);
            ctx.assertNull(result.getPayload());
        }));
    }

    private BaseRegistrationService getRegistrationService(final int status, final JsonObject data) {
        return new BaseRegistrationService() {

            @Override
            public void updateDevice(final String tenantId, final String deviceId, final JsonObject otherKeys, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
            }

            @Override
            public void removeDevice(final String tenantId, final String deviceId, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
            }

            @Override
            public void getDevice(final String tenantId, final String deviceId, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(status, data)));
            }

            @Override
            public void findDevice(final String tenantId, final String key, final String value, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
            }

            @Override
            public void addDevice(final String tenantId, final String deviceId, JsonObject otherKeys, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
            }
        };
    }
}