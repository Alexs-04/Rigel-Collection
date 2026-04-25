import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {AuthProvider} from './context/AuthProvider'
import ErrorBoundary from './components/ErrorBoundary'
import {AppSettingsProvider} from './context/AppSettingsContext'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <AppSettingsProvider>
            <ErrorBoundary>
                <AuthProvider>
                    <App/>
                </AuthProvider>
            </ErrorBoundary>
        </AppSettingsProvider>
    </StrictMode>,
)
