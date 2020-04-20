package android.hardware.biometrics;

/**
 * Communication channel from
 *   1) BiometricDialogImpl (SysUI) back to BiometricService
 *   2) <Biometric>Service back to BiometricService
 * Receives messages from the above and does some handling before forwarding to BiometricPrompt
 * via IBiometricServiceReceiver.
 * @hide
 */
interface IBiometricServiceReceiverInternal {
    // Notify BiometricService that authentication was successful. If user confirmation is required,
    // the auth token must be submitted into KeyStore.
    void onAuthenticationSucceeded(boolean requireConfirmation, in byte[] token);
    // Notify BiometricService that an error has occurred.
    void onAuthenticationFailed(int cookie, boolean requireConfirmation);
    // Notify BiometricService than an error has occured. Forward to the correct receiver depending
    // on the cookie.
    void onError(int cookie, int error, String message);
    // Notifies that a biometric has been acquired.
    void onAcquired(int acquiredInfo, String message);
    // Notifies that the SystemUI dialog has been dismissed.
    void onDialogDismissed(int reason);
    // Notifies that the user has pressed the "try again" button on SystemUI
    void onTryAgainPressed();
}