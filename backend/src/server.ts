import express from "express";
import type { ErrorRequestHandler } from "express";
import { config } from "./config.js";
import authRoutes from "./routes/auth.routes.js";
import profileRoutes from "./routes/profile.routes.js";

const app = express();

app.use(express.json());
app.use("/api/auth", authRoutes);
app.use("/api/profile", profileRoutes);

app.get("/api/health", (_req, res) => {
    res.json({ message: "Backend funcionando correctamente" });
});

app.use((_req, res) => {
    res.status(404).json({ message: "Ruta no encontrada" });
});

const handleError: ErrorRequestHandler = (error, _req, res, next) => {
    if (res.headersSent) {
        return next(error);
    }

    switch (error.type) {
        case "entity.parse.failed":
            res.status(400).json({ message: "El cuerpo debe contener JSON válido" });
            return;
        case "entity.too.large":
            res.status(413).json({ message: "El cuerpo de la solicitud es demasiado grande" });
            return;
        case "encoding.unsupported":
        case "charset.unsupported":
            res.status(415).json({ message: "Codificación de la solicitud no admitida" });
            return;
        case "request.aborted":
        case "request.size.invalid":
            res.status(400).json({ message: "Solicitud incompleta o inválida" });
            return;
        default:
            console.error("Error al procesar una solicitud");
            res.status(500).json({ message: "Error interno del servidor" });
    }
};

app.use(handleError);

app.listen(config.port, () => {
    console.log(`Servidor ejecutándose en http://localhost:${config.port}`);
});
